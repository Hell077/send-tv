package org.sendtv.receiver

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sendtv.receiver.network.HttpRequest
import org.sendtv.receiver.network.HttpResponse
import org.sendtv.receiver.network.ReceiverApi
import org.sendtv.receiver.security.DeviceStore
import org.sendtv.receiver.security.PairingManager
import org.sendtv.receiver.security.TrustedDevice

class ReceiverApiTest {
    private val store =
        object : DeviceStore {
            override fun load() = emptyList<TrustedDevice>()

            override fun save(devices: List<TrustedDevice>) = Unit
        }
    private val pairing = PairingManager(store)
    private val queued = mutableListOf<() -> Unit>()
    private var launches = 0
    private var launchError: String? = null
    private val api =
        ReceiverApi("192.168.1.5:8765", pairing, { "test-tv" to "Living Room TV" }, { "asset".toByteArray() }, { queued.add(it) }, {
            launches++
            launchError
        })

    private fun request(
        method: String,
        path: String,
        body: String = "",
        token: String? = null,
        extra: Map<String, String> = emptyMap(),
    ): HttpResponse {
        val headers =
            mutableMapOf("host" to "192.168.1.5:8765", "origin" to "http://192.168.1.5:8765", "content-type" to "application/json")
        token?.let { headers["authorization"] = "Bearer $it" }
        headers.putAll(extra)
        return api.handle(HttpRequest(method, path, headers, body, "peer"))
    }

    private fun body(response: HttpResponse) = JsonParser.parseString(response.body.toString(Charsets.UTF_8)).asJsonObject

    private fun authorize(): String {
        val pair = pairing.request(pairing.challenge().nonce, "Phone", "local-test")
        pairing.decide(pair.id, true)
        return pair.accessToken!!
    }

    @Test fun pairingContractRequiresTvAndSeparatePollingCredential() {
        val nonce = pairing.challenge().nonce
        val response = request("POST", "/v1/pair", """{"challenge":"$nonce","deviceName":"Phone"}""")
        assertEquals(202, response.status)
        val pending = body(response)
        assertEquals(setOf("requestId", "pollToken", "status", "expiresIn", "verificationCode"), pending.keySet())
        val id = pending["requestId"].asString
        val poll = pending["pollToken"].asString
        assertEquals(401, request("GET", "/v1/pair/$id").status)
        assertEquals("pending", body(request("GET", "/v1/pair/$id", token = poll))["status"].asString)
        assertEquals(401, request("POST", "/v1/open", "{}", poll).status)
        pairing.decide(id, true)
        val approved = body(request("GET", "/v1/pair/$id", token = poll))
        assertEquals(setOf("status", "accessToken", "deviceId"), approved.keySet())
        assertEquals("approved", approved["status"].asString)
        assertEquals(204, request("DELETE", "/v1/pair/$id", token = poll).status)
        assertEquals("approved", body(request("GET", "/v1/pair/$id", token = poll))["status"].asString)
        assertEquals(0, launches)
    }

    @Test fun unauthorizedAndMalformedRequestsNeverLaunch() {
        assertEquals(401, request("POST", "/v1/open", "{}").status)
        val token = authorize()
        for (invalid in listOf(
            "{}",
            "[]",
            "null",
            "{'url':'https://example.com','requestId':'12345678'}",
            """{"url":"https://example.com","requestId":null}""",
            """{"url":"https://example.com","url":"https://evil.com","requestId":"12345678"}""",
            """{"url":"https://example.com","requestId":"12345678","extra":true}""",
            """{"url":"https://example.com","requestId":"12345678"} {}""",
        )) {
            assertEquals(invalid, 400, request("POST", "/v1/open", invalid, token).status)
        }
        assertEquals(400, request("POST", "/v1/open", """{"url":"javascript:alert(1)","requestId":"12345678"}""", token).status)
        assertEquals(415, request("POST", "/v1/open", "{}", token, mapOf("content-type" to "text/plain")).status)
        assertTrue(queued.isEmpty())
        assertEquals(0, launches)
    }

    @Test fun rejectsForeignOriginsRebindingAndOwnerRoutes() {
        val token = authorize()
        assertEquals(403, request("POST", "/v1/open", "{}", token, mapOf("origin" to "https://evil.example")).status)
        assertEquals(403, request("GET", "/v1/info", extra = mapOf("host" to "evil.example:8765")).status)
        assertEquals(403, request("GET", "/v1/info", extra = mapOf("sec-fetch-site" to "cross-site")).status)
        assertEquals(403, api.handle(HttpRequest("POST", "/v1/pair", mapOf("host" to "192.168.1.5:8765"), "{}", "peer")).status)
        for (path in listOf("/v1/pair/challenge", "/v1/devices/id", "/proxy", "/../../etc/passwd", "/file://secret")) {
            assertEquals(404, request("GET", path).status)
        }
        assertEquals(0, launches)
    }

    @Test fun idempotencyAndOwnerIsolationPreserveOneHandoff() {
        val token = authorize()
        val data = """{"url":"https://example.com/?secret=fixture","requestId":"12345678"}"""
        val first = body(request("POST", "/v1/open", data, token))
        val second = body(request("POST", "/v1/open", data, token))
        assertEquals(first, second)
        assertEquals(1, queued.size)
        assertEquals(409, request("POST", "/v1/open", data.replace("example.com", "other.example"), token).status)
        val operation = first["operationId"].asString
        assertEquals(401, request("GET", "/v1/operations/$operation").status)
        assertEquals(404, request("GET", "/v1/operations/$operation", token = authorize()).status)
        queued.removeAt(0).invoke()
        val state = body(request("GET", "/v1/operations/$operation", token = token))
        assertEquals("launched", state["status"].asString)
        assertFalse(state.toString().contains("secret"))
        assertEquals(1, launches)
    }

    @Test fun revocationCancelsQueuedSideEffectsAndMissingBrowserIsReported() {
        val token = authorize()
        request("POST", "/v1/open", """{"url":"https://example.com","requestId":"12345678"}""", token)
        pairing.revoke(null)
        queued.removeAt(0).invoke()
        assertEquals(0, launches)
        val next = authorize()
        launchError = "NO_BROWSER"
        val id = body(request("POST", "/v1/open", """{"url":"https://example.com","requestId":"abcdefgh"}""", next))["operationId"].asString
        queued.removeAt(0).invoke()
        val state = body(request("GET", "/v1/operations/$id", token = next))
        assertEquals("failed", state["status"].asString)
        assertEquals("NO_BROWSER", state["errorCode"].asString)
    }
}
