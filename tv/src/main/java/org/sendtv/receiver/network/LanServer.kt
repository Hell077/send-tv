package org.sendtv.receiver.network

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/** Deliberately small HTTP/1.1 subset: one fixed-length request per connection. */
class LanServer(
    address: InetAddress,
    port: Int,
    private val handle: (HttpRequest) -> HttpResponse,
) : AutoCloseable {
    private val listener = ServerSocket()
    private val sockets = ConcurrentHashMap.newKeySet<Socket>()
    private val workers = ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS, ArrayBlockingQueue(8))
    private val acceptThread: Thread
    val port: Int get() = listener.localPort

    init {
        require(address is Inet4Address && (address.isSiteLocalAddress || address.isLoopbackAddress))
        require(port == 0 || port in 1024..65535)
        listener.reuseAddress = true
        listener.bind(InetSocketAddress(address, port), 8)
        acceptThread =
            thread(name = "sendtv-listener", isDaemon = true) {
                while (!listener.isClosed) {
                    val socket =
                        try {
                            listener.accept()
                        } catch (_: Exception) {
                            break
                        }
                    sockets.add(socket)
                    try {
                        workers.execute { serve(socket) }
                    } catch (_: RuntimeException) {
                        sockets.remove(socket)
                        socket.close()
                    }
                }
            }
    }

    private fun serve(socket: Socket) {
        socket.use {
            try {
                socket.soTimeout = 5000
                val request = parse(socket)
                respond(socket, handle(request))
            } catch (failure: BadRequest) {
                runCatching {
                    respond(
                        socket,
                        HttpResponse(failure.status, "{\"code\":\"INVALID_HTTP\",\"message\":\"Invalid HTTP request\"}".toByteArray()),
                    )
                }
            } catch (_: Exception) {
                // No exception messages: malformed input can contain URL/token secrets.
            } finally {
                sockets.remove(socket)
            }
        }
    }

    private class BadRequest(
        val status: Int = 400,
    ) : RuntimeException()

    private fun parse(socket: Socket): HttpRequest {
        val input = BufferedInputStream(socket.getInputStream())
        var headerBytes = 0
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)

        fun line(): String {
            val buffer = ByteArrayOutputStream()
            while (true) {
                if (++headerBytes > 8192) throw BadRequest(431)
                if (System.nanoTime() >= deadline) throw BadRequest(408)
                val byte = input.read()
                if (byte < 0 || byte == 10 || byte > 126 || (byte < 32 && byte != 13)) throw BadRequest()
                if (byte == 13) {
                    if (input.read() != 10) throw BadRequest()
                    headerBytes++
                    return buffer.toString("US-ASCII")
                }
                buffer.write(byte)
            }
        }
        val start = line().split(' ')
        if (start.size != 3 || start[2] != "HTTP/1.1" || start[0] !in setOf("GET", "POST", "DELETE")) throw BadRequest()
        if (!Regex("/[A-Za-z0-9/._-]*").matches(start[1]) || ".." in start[1]) throw BadRequest()
        val headers = mutableMapOf<String, String>()
        while (true) {
            val header = line()
            if (header.isEmpty()) break
            val colon = header.indexOf(':')
            if (colon < 1) throw BadRequest()
            val key = header.substring(0, colon).lowercase()
            if (!Regex("[a-z0-9-]+").matches(key) || key in headers) throw BadRequest()
            headers[key] = header.substring(colon + 1).trim()
        }
        if ("host" !in headers || "transfer-encoding" in headers || "expect" in headers) throw BadRequest()
        val length =
            headers["content-length"]?.let {
                if (!Regex("[0-9]{1,6}").matches(it)) throw BadRequest()
                it.toInt()
            } ?: if (start[0] == "POST") throw BadRequest(411) else 0
        if (length > 16_384) throw BadRequest(413)
        if (start[0] != "POST" && length != 0) throw BadRequest()
        val body = ByteArray(length)
        var position = 0
        while (position < length) {
            if (System.nanoTime() >= deadline) throw BadRequest(408)
            val read = input.read(body, position, length - position)
            if (read < 0) throw BadRequest()
            position += read
        }
        val text =
            try {
                Charsets.UTF_8
                    .newDecoder()
                    .decode(java.nio.ByteBuffer.wrap(body))
                    .toString()
            } catch (_: Exception) {
                throw BadRequest()
            }
        return HttpRequest(start[0], start[1], headers, text, socket.inetAddress.hostAddress ?: "unknown")
    }

    private fun respond(
        socket: Socket,
        response: HttpResponse,
    ) {
        val headers =
            "HTTP/1.1 ${response.status} Response\r\n" +
                "Content-Type: ${response.contentType}\r\nContent-Length: ${response.body.size}\r\n" +
                "Connection: close\r\nCache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\n" +
                "Referrer-Policy: no-referrer\r\nX-Frame-Options: DENY\r\n" +
                "Content-Security-Policy: default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self'; connect-src 'self'; base-uri 'none'; frame-ancestors 'none'; form-action 'self'\r\n\r\n"
        socket.getOutputStream().apply {
            write(headers.toByteArray(Charsets.US_ASCII))
            write(response.body)
            flush()
        }
    }

    override fun close() {
        listener.close()
        sockets.forEach { runCatching { it.close() } }
        workers.shutdownNow()
        acceptThread.join(1000)
    }
}
