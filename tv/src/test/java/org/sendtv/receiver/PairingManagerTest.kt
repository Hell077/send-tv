package org.sendtv.receiver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sendtv.receiver.security.DeviceStore
import org.sendtv.receiver.security.PairingManager
import org.sendtv.receiver.security.ProtocolFailure
import org.sendtv.receiver.security.TrustedDevice

class PairingManagerTest {
    private class MemoryStore : DeviceStore {
        var records = emptyList<TrustedDevice>()
        var failWrites = false

        override fun load() = records

        override fun save(devices: List<TrustedDevice>) {
            if (failWrites) error("test storage failure")
            records = devices.toList()
        }
    }

    private var time = 0L
    private val store = MemoryStore()
    private val manager = PairingManager(store) { time }

    private fun failure(
        code: String,
        action: () -> Unit,
    ) {
        val failure = assertThrows(ProtocolFailure::class.java, action)
        assertEquals(code, failure.code)
    }

    @Test fun requiresTvApprovalAndScopedPolling() {
        val request = manager.request(manager.challenge().nonce, "Phone", "peer")
        assertNull(manager.poll(request.id, request.pollToken).accessToken)
        failure("UNAUTHORIZED") { manager.poll(request.id, "wrong") }
        failure("UNAUTHORIZED") { manager.authenticate(request.pollToken) }
        manager.decide(request.id, true)
        val token = manager.poll(request.id, request.pollToken).accessToken!!
        assertEquals("Phone", manager.authenticate(token).name)
        assertFalse(
            store.records
                .single()
                .tokenHash
                .contains(token),
        )
        assertEquals(
            64,
            store.records
                .single()
                .tokenHash.length,
        )
        assertEquals("Phone", PairingManager(store).authenticate(token).name)
    }

    @Test fun rejectsExpiredReplayedAndMalformedPairing() {
        val nonce = manager.challenge().nonce
        time = 60_000
        failure("INVALID_CHALLENGE") { manager.request(nonce, "Phone", "peer") }
        val next = manager.challenge().nonce
        failure("INVALID_DEVICE_NAME") { manager.request(next, "\n", "peer") }
        val request = manager.request(next, "Phone", "peer")
        failure("INVALID_CHALLENGE") { manager.request(next, "Other", "peer") }
        time += 60_000
        manager.decide(request.id, true)
        assertTrue(manager.devices().isEmpty())
        failure("UNAUTHORIZED") { manager.poll(request.id, request.pollToken) }
    }

    @Test fun denialCancellationRevocationAndRestartFailClosed() {
        val denied = manager.request(manager.challenge().nonce, "Phone", "peer")
        manager.decide(denied.id, false)
        manager.decide(denied.id, true)
        assertNull(denied.accessToken)
        val cancelled = manager.request(manager.challenge().nonce, "Phone", "peer")
        failure("UNAUTHORIZED") { manager.cancel(cancelled.id, "wrong") }
        manager.cancel(cancelled.id, cancelled.pollToken)
        assertEquals("denied", cancelled.status)
        val approved = manager.request(manager.challenge().nonce, "Phone", "peer")
        manager.decide(approved.id, true)
        val token = approved.accessToken!!
        manager.revoke(approved.deviceId)
        failure("UNAUTHORIZED") { manager.authenticate(token) }
        assertNull(manager.poll(approved.id, approved.pollToken).accessToken)
        manager.endSession()
        failure("UNAUTHORIZED") { manager.poll(approved.id, approved.pollToken) }
    }

    @Test fun limitsPairRequestsAndStorageFailureNeverAuthorizes() {
        repeat(5) { failure("INVALID_CHALLENGE") { manager.request("invalid", "Phone", "peer") } }
        failure("RATE_LIMITED") { manager.request(manager.challenge().nonce, "Phone", "peer") }
        time = 60_000
        val request = manager.request(manager.challenge().nonce, "Phone", "peer")
        store.failWrites = true
        assertThrows(IllegalStateException::class.java) { manager.decide(request.id, true) }
        assertNull(request.accessToken)
        assertTrue(manager.devices().isEmpty())
    }
}
