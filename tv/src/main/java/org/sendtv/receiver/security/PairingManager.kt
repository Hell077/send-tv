package org.sendtv.receiver.security

import org.sendtv.receiver.ReceiverName
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class ProtocolFailure(
    val status: Int,
    val code: String,
) : RuntimeException(code)

data class TrustedDevice(
    val id: String,
    val name: String,
    val tokenHash: String,
)

interface DeviceStore {
    fun load(): List<TrustedDevice>

    /** Must persist atomically; throwing leaves the in-memory authorization unchanged. */
    fun save(devices: List<TrustedDevice>)
}

class Challenge(
    val nonce: String,
    val expiresAt: Long,
)

class PairRequest(
    val id: String,
    val deviceName: String,
    val pollToken: String,
    val verificationCode: String,
    val expiresAt: Long,
) {
    var status: String = "pending"
        internal set
    var accessToken: String? = null
        internal set
    var deviceId: String? = null
        internal set
}

/** Bounded monotonic fixed-window limiter. Full buckets fail closed. */
class RateLimit(
    private val count: Int,
    private val now: () -> Long,
    private val maxKeys: Int = 64,
) {
    private val buckets = mutableMapOf<String, Pair<Long, Int>>()

    @Synchronized
    fun allow(key: String): Boolean {
        val time = now()
        buckets.entries.removeAll { time - it.value.first >= 60_000 }
        val old = buckets[key]
        if (old == null && buckets.size >= maxKeys) return false
        if (old != null && old.second >= count) return false
        buckets[key] = (old?.first ?: time) to ((old?.second ?: 0) + 1)
        return true
    }
}

/** TV-only approval/revocation methods are never routed as HTTP endpoints. */
class PairingManager(
    private val store: DeviceStore,
    private val now: () -> Long = { System.nanoTime() / 1_000_000 },
) {
    private val random = SecureRandom()
    private var challenge: Challenge? = null
    private val requests = linkedMapOf<String, PairRequest>()
    private var devices = store.load().take(32)
    private val pairLimit = RateLimit(5, now)
    private val globalLimit = RateLimit(20, now)

    private fun secret(): String =
        ByteArray(32).also(random::nextBytes).let {
            Base64.getUrlEncoder().withoutPadding().encodeToString(it)
        }

    private fun purge() {
        requests.entries.removeAll { it.value.expiresAt <= now() }
    }

    @Synchronized
    fun challenge(): Challenge {
        val old = challenge
        if (old != null && now() < old.expiresAt) return old
        return Challenge(secret(), now() + 60_000).also { challenge = it }
    }

    @Synchronized
    fun request(
        nonce: String,
        deviceName: String,
        source: String,
    ): PairRequest {
        purge()
        if (!globalLimit.allow("pair") || !pairLimit.allow(source)) throw ProtocolFailure(429, "RATE_LIMITED")
        if (!ReceiverName.isValid(deviceName)) throw ProtocolFailure(400, "INVALID_DEVICE_NAME")
        val active = challenge
        if (active == null || now() >= active.expiresAt || !equal(active.nonce, nonce)) {
            throw ProtocolFailure(400, "INVALID_CHALLENGE")
        }
        if (requests.size >= 8 || devices.size >= 32) throw ProtocolFailure(429, "DEVICE_LIMIT")
        challenge = null // consume at submission, including requests later denied by TV
        return PairRequest(secret(), deviceName, secret(), "%06d".format(random.nextInt(1_000_000)), now() + 60_000)
            .also { requests[it.id] = it }
    }

    @Synchronized
    fun poll(
        id: String,
        credential: String,
    ): PairRequest {
        purge()
        val request = requests[id]
        if (request == null || !equal(request.pollToken, credential)) throw ProtocolFailure(401, "UNAUTHORIZED")
        return request
    }

    @Synchronized
    fun pending(): List<PairRequest> {
        purge()
        return requests.values.filter { it.status == "pending" }
    }

    @Synchronized
    fun decide(
        id: String,
        allow: Boolean,
    ) {
        purge()
        val request = requests[id] ?: return
        if (request.status != "pending") return
        if (!allow) {
            request.status = "denied"
            return
        }
        if (devices.size >= 32) {
            request.status = "denied"
            return
        }
        val token = secret()
        val device = TrustedDevice(secret(), request.deviceName, hash(token))
        val updated = devices + device
        store.save(updated)
        devices = updated
        request.deviceId = device.id
        request.accessToken = token
        request.status = "approved"
    }

    @Synchronized
    fun cancel(
        id: String,
        credential: String,
    ) {
        val request = poll(id, credential)
        if (request.status == "pending") request.status = "denied"
    }

    @Synchronized
    fun authenticate(token: String): TrustedDevice {
        if (token.length != 43) throw ProtocolFailure(401, "UNAUTHORIZED")
        val digest = hash(token)
        return devices.firstOrNull { equal(it.tokenHash, digest) } ?: throw ProtocolFailure(401, "UNAUTHORIZED")
    }

    @Synchronized
    fun devices(): List<TrustedDevice> = devices.toList()

    @Synchronized
    fun revoke(id: String?) {
        val updated = if (id == null) emptyList() else devices.filterNot { it.id == id }
        store.save(updated)
        devices = updated
        requests.values.filter { id == null || it.deviceId == id }.forEach {
            it.accessToken = null
            it.status = "denied"
        }
    }

    @Synchronized
    fun endSession() {
        requests.clear()
        challenge = null
    }

    companion object {
        private fun equal(
            a: String,
            b: String,
        ): Boolean = MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

        private fun hash(value: String): String =
            MessageDigest
                .getInstance("SHA-256")
                .digest(value.toByteArray())
                .joinToString("") { "%02x".format(it) }
    }
}
