package org.sendtv.receiver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.sendtv.receiver.open.UrlValidator
import org.sendtv.receiver.security.ProtocolFailure

class UrlValidatorTest {
    @Test fun acceptsOnlyUnmodifiedHttpUrls() {
        listOf(
            "https://example.com/?secret=fixture#fragment",
            "http://192.168.1.2:8080/",
            "HTTPS://example.com/",
            "http://[::1]/",
        ).forEach {
            assertEquals(it, UrlValidator.validate(it))
        }
    }

    @Test fun rejectsMalformedSchemesCredentialsAndOversizeInputs() {
        listOf(
            "",
            "https:///path",
            "https://",
            "https://example.com:0",
            "https://example.com:65536",
            "https://u:p@example.com",
            "https://u%40example.com",
            "https://example.com\\@evil.com",
            " https://example.com",
            "https://example.com/\nsecret",
            "https://example.com/%zz",
            "https://example.com/" + "x".repeat(4096),
            "javascript:alert(1)",
            "file:///tmp/x",
            "content://x",
            "intent://x",
            "https://example.com /x",
        ).forEach {
            assertThrows(it, ProtocolFailure::class.java) { UrlValidator.validate(it) }
        }
    }
}
