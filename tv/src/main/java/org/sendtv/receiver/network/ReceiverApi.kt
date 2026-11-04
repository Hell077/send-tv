package org.sendtv.receiver.network

import com.google.gson.Gson
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import org.sendtv.receiver.open.UrlValidator
import org.sendtv.receiver.security.PairingManager
import org.sendtv.receiver.security.ProtocolFailure
import org.sendtv.receiver.security.RateLimit
import java.io.StringReader
import java.security.MessageDigest
import java.util.UUID

class HttpRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String>,
    val body: String,
    val peer: String,
)

class HttpResponse(
    val status: Int,
    val body: ByteArray,
    val contentType: String = "application/json; charset=utf-8",
)

class ReceiverApi(
    private val authority: String,
    private val pairing: PairingManager,
    private val identity: () -> Pair<String, String>,
    private val assets: (String) -> ByteArray?,
    private val schedule: (() -> Unit) -> Unit,
    private val launch: (String) -> String?,
    private val now: () -> Long = { System.nanoTime() / 1_000_000 },
) {
    private class Operation(
        val id: String,
        val owner: String,
        val key: String,
        val digest: String,
        val created: Long,
    ) {
        var status = "accepted"
        var errorCode: String? = null
    }

    private val gson = Gson()
    private val traffic = RateLimit(180, now)
    private val opens = RateLimit(30, now)
    private val operations = linkedMapOf<String, Operation>()
    private val session = UUID.randomUUID().toString()
    private var sequence = 0L

    @Synchronized
    fun handle(request: HttpRequest): HttpResponse =
        try {
            if (request.headers["host"] != authority) throw ProtocolFailure(403, "INVALID_HOST")
            val origin = request.headers["origin"]
            if ((origin != null && origin != "http://$authority") ||
                (request.method != "GET" && origin == null) ||
                request.headers["sec-fetch-site"] in setOf("cross-site", "same-site")
            ) {
                throw ProtocolFailure(403, "INVALID_ORIGIN")
            }
            if (!traffic.allow(request.peer)) throw ProtocolFailure(429, "RATE_LIMITED")
            route(request)
        } catch (failure: ProtocolFailure) {
            error(failure.status, failure.code)
        } catch (_: Exception) {
            error(500, "INTERNAL_ERROR")
        }

    private fun route(request: HttpRequest): HttpResponse {
        val path = request.path
        if (request.method == "GET" && path in setOf("/", "/app.js", "/app.css")) {
            val name = if (path == "/") "index.html" else path.drop(1)
            val data = assets(name) ?: throw ProtocolFailure(404, "NOT_FOUND")
            val type =
                when (name) {
                    "app.js" -> "text/javascript; charset=utf-8"
                    "app.css" -> "text/css; charset=utf-8"
                    else -> "text/html; charset=utf-8"
                }
            return HttpResponse(200, data, type)
        }
        if (request.method == "GET" && path == "/v1/info") {
            val (id, name) = identity()
            return json(200, mapOf("protocolVersion" to 1, "deviceId" to id, "name" to name, "capabilities" to listOf("open-url")))
        }
        if (request.method == "POST" && path == "/v1/pair") {
            val body = strings(request, setOf("challenge", "deviceName"))
            if (body.getValue("challenge").length != 43) throw ProtocolFailure(400, "INVALID_CHALLENGE")
            val pair = pairing.request(body.getValue("challenge"), body.getValue("deviceName"), request.peer)
            return json(
                202,
                mapOf(
                    "requestId" to pair.id,
                    "pollToken" to pair.pollToken,
                    "status" to "pending",
                    "expiresIn" to 60,
                    "verificationCode" to pair.verificationCode,
                ),
            )
        }
        if (Regex("/v1/pair/[A-Za-z0-9_-]{43}").matches(path) && request.method in setOf("GET", "DELETE")) {
            val id = path.removePrefix("/v1/pair/")
            val credential = bearer(request)
            if (request.method == "DELETE") {
                pairing.cancel(id, credential)
                return HttpResponse(204, ByteArray(0))
            }
            val pair = pairing.poll(id, credential)
            val body = mutableMapOf<String, Any>("status" to pair.status)
            if (pair.status == "approved") {
                body["accessToken"] = pair.accessToken!!
                body["deviceId"] = pair.deviceId!!
            }
            return json(200, body)
        }
        if (path == "/v1/open" && request.method == "POST") {
            val token = bearer(request)
            val device = pairing.authenticate(token)
            val body = strings(request, setOf("url", "requestId"))
            val url = UrlValidator.validate(body.getValue("url"))
            val key = body.getValue("requestId")
            if (key.length !in 8..128 || key.any { it.isISOControl() }) throw ProtocolFailure(400, "INVALID_REQUEST")
            operations.entries.removeAll { now() - it.value.created >= 300_000 }
            val digest = MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
            val existing = operations.values.firstOrNull { it.owner == device.id && it.key == key }
            if (existing != null) {
                if (existing.digest != digest) throw ProtocolFailure(409, "REQUEST_CONFLICT")
                return json(202, mapOf("operationId" to existing.id, "status" to "accepted"))
            }
            if (!opens.allow(device.id) || operations.size >= 128) throw ProtocolFailure(429, "RATE_LIMITED")
            val operation = Operation("$session-${++sequence}", device.id, key, digest, now())
            operations[operation.id] = operation
            schedule {
                synchronized(this) {
                    try {
                        pairing.authenticate(token) // re-check revocation immediately before side effect
                        operation.status = "launching"
                        operation.errorCode = launch(url)
                        operation.status = if (operation.errorCode == null) "launched" else "failed"
                    } catch (_: ProtocolFailure) {
                        operation.status = "failed"
                        operation.errorCode = "REVOKED"
                    } catch (_: Exception) {
                        operation.status = "failed"
                        operation.errorCode = "LAUNCH_FAILED"
                    }
                }
            }
            return json(202, mapOf("operationId" to operation.id, "status" to "accepted"))
        }
        if (path.startsWith("/v1/operations/") && request.method == "GET") {
            val device = pairing.authenticate(bearer(request))
            val operation = operations[path.removePrefix("/v1/operations/")]
            if (operation == null || operation.owner != device.id || now() - operation.created >= 300_000) {
                throw ProtocolFailure(404, "NOT_FOUND")
            }
            val body = mutableMapOf<String, Any>("operationId" to operation.id, "status" to operation.status)
            operation.errorCode?.let { body["errorCode"] = it }
            return json(200, body)
        }
        // No arbitrary asset paths, challenge retrieval, TV approval or proxy routes.
        throw ProtocolFailure(404, "NOT_FOUND")
    }

    private fun bearer(request: HttpRequest): String {
        val value = request.headers["authorization"] ?: throw ProtocolFailure(401, "UNAUTHORIZED")
        if (!value.startsWith("Bearer ")) throw ProtocolFailure(401, "UNAUTHORIZED")
        return value.removePrefix("Bearer ")
    }

    private fun strings(
        request: HttpRequest,
        fields: Set<String>,
    ): Map<String, String> {
        if (request.headers["content-type"]?.lowercase() !in setOf("application/json", "application/json; charset=utf-8")) {
            throw ProtocolFailure(415, "JSON_REQUIRED")
        }
        try {
            val result = mutableMapOf<String, String>()
            JsonReader(StringReader(request.body)).use { reader ->
                reader.strictness = Strictness.STRICT
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    if (name !in fields || name in result || reader.peek() != JsonToken.STRING) throw IllegalArgumentException()
                    result[name] = reader.nextString()
                }
                reader.endObject()
                if (reader.peek() != JsonToken.END_DOCUMENT || result.keys != fields) throw IllegalArgumentException()
            }
            return result
        } catch (_: Exception) {
            throw ProtocolFailure(400, "INVALID_JSON")
        }
    }

    private fun json(
        status: Int,
        value: Any,
    ): HttpResponse = HttpResponse(status, gson.toJson(value).toByteArray(Charsets.UTF_8))

    private fun error(
        status: Int,
        code: String,
    ): HttpResponse =
        json(
            status,
            mapOf(
                "code" to code,
                "message" to code.lowercase().replace('_', ' '),
            ),
        )
}
