package org.sendtv.receiver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sendtv.receiver.network.HttpResponse
import org.sendtv.receiver.network.LanServer
import java.net.InetAddress
import java.net.Socket

class LanServerTest {
    private fun exchange(
        server: LanServer,
        request: String,
    ): String =
        Socket("127.0.0.1", server.port).use { socket ->
            socket.soTimeout = 6000
            socket.getOutputStream().write(request.toByteArray())
            socket.shutdownOutput()
            socket.getInputStream().readBytes().toString(Charsets.UTF_8)
        }

    @Test fun servesRealHttpAndClosesListenerOnShutdown() {
        val server = LanServer(InetAddress.getByName("127.0.0.1"), 0) { HttpResponse(200, "ok".toByteArray()) }
        val port = server.port
        val response = exchange(server, "GET / HTTP/1.1\r\nHost: 127.0.0.1:$port\r\n\r\n")
        assertTrue(response.startsWith("HTTP/1.1 200"))
        assertTrue(response.endsWith("ok"))
        assertTrue(response.contains("Cache-Control: no-store"))
        assertTrue(response.contains("Content-Security-Policy:"))
        assertFalse(response.contains("Access-Control-Allow-Origin"))
        server.close()
        assertThrows(Exception::class.java) { Socket("127.0.0.1", port).close() }
    }

    @Test fun rejectsSmugglingOversizeAndProxyRequestsBeforeHandler() {
        var handled = 0
        LanServer(InetAddress.getByName("127.0.0.1"), 0) {
            handled++
            HttpResponse(200, ByteArray(0))
        }.use { server ->
            val malformed =
                listOf(
                    "GET http://127.0.0.1/ HTTP/1.1\r\nHost: x\r\n\r\n",
                    "GET /../secret HTTP/1.1\r\nHost: x\r\n\r\n",
                    "GET / HTTP/1.1\r\nHost: x\r\nHost: y\r\n\r\n",
                    "GET / HTTP/1.1\nHost: x\n\n",
                    "POST /v1/open HTTP/1.1\r\nHost: x\r\nContent-Length: 2\r\nTransfer-Encoding: chunked\r\n\r\n{}",
                    "POST /v1/open HTTP/1.1\r\nHost: x\r\nContent-Length: 20000\r\n\r\n",
                    "POST /v1/open HTTP/1.1\r\nHost: x\r\nContent-Length: -1\r\n\r\n",
                    "POST /v1/open HTTP/1.1\r\nHost: x\r\nContent-Length: 4\r\n\r\n{}",
                    "GET / HTTP/1.1\r\nX-Big: " + "x".repeat(9000) + "\r\n\r\n",
                )
            malformed.forEach { assertTrue(exchange(server, it).startsWith("HTTP/1.1 4")) }
            assertEquals(0, handled)
        }
    }

    @Test fun refusesWildcardAndPublicBindAddresses() {
        for (address in listOf("0.0.0.0", "8.8.8.8", "::")) {
            assertThrows(
                IllegalArgumentException::class.java,
            ) { LanServer(InetAddress.getByName(address), 0) { HttpResponse(200, ByteArray(0)) } }
        }
    }
}
