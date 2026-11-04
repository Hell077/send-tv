package org.sendtv.receiver

import org.sendtv.receiver.network.LanServer
import org.sendtv.receiver.network.ReceiverApi
import org.sendtv.receiver.security.DeviceStore
import org.sendtv.receiver.security.PairingManager
import org.sendtv.receiver.security.TrustedDevice
import java.io.File
import java.net.InetAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Test-only TV stand-in. Not packaged in the APK; no remote approval endpoint. */
object ReceiverBrowserHarness {
    @JvmStatic
    fun main(args: Array<String>) {
        val assets = File(args[0])
        val challengeFile = File(args[1])
        val pairing =
            PairingManager(
                object : DeviceStore {
                    override fun load() = emptyList<TrustedDevice>()

                    override fun save(devices: List<TrustedDevice>) = Unit
                },
            )
        lateinit var api: ReceiverApi
        val server = LanServer(InetAddress.getByName("127.0.0.1"), 18765) { api.handle(it) }
        api =
            ReceiverApi("127.0.0.1:18765", pairing, { "browser-fixture-tv" to "Test Living Room TV" }, {
                File(
                    assets,
                    it,
                ).readBytes()
            }, { it() }, {
                when {
                    it.endsWith("/no-browser") -> "NO_BROWSER"
                    it.endsWith("/background") -> "RETURN_TO_RECEIVER"
                    else -> null
                }
            })
        val clock = Executors.newSingleThreadScheduledExecutor()
        clock.scheduleAtFixedRate({
            pairing.pending().forEach { pairing.decide(it.id, !it.deviceName.startsWith("Denied")) }
            val challenge = pairing.challenge().nonce
            if (!challengeFile.exists() || challengeFile.readText() != challenge) challengeFile.writeText(challenge)
        }, 0, 50, TimeUnit.MILLISECONDS)
        Runtime.getRuntime().addShutdownHook(
            Thread {
                api.close()
                server.close()
                clock.shutdownNow()
                challengeFile.delete()
            },
        )
        println("Test-only receiver ready on loopback; Android handoff is simulated")
        CountDownLatch(1).await()
    }
}
