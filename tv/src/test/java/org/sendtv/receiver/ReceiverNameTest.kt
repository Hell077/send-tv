package org.sendtv.receiver

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiverNameTest {
    @Test
    fun acceptsVisibleNamesAtTheLengthBoundary() {
        listOf("Living Room TV", "TV кухня", "A", "x".repeat(48)).forEach {
            assertTrue(it, ReceiverName.isValid(it))
        }
    }

    @Test
    fun rejectsBlankOversizedAndHiddenCharacters() {
        listOf("", " ", " TV", "TV ", "x".repeat(49), "TV\nroom", "TV\u0000", "TV\u202E", "TV\u200B")
            .forEach { assertFalse(ReceiverName.isValid(it)) }
    }
}
