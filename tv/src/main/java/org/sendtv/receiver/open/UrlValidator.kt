package org.sendtv.receiver.open

import org.sendtv.receiver.security.ProtocolFailure
import java.net.URI

object UrlValidator {
    fun validate(value: String): String {
        val uri = runCatching { URI(value) }.getOrNull()
        if (value.length !in 1..4096 ||
            value.any { it.isWhitespace() || it.isISOControl() } ||
            '\\' in value ||
            uri == null ||
            uri.scheme?.lowercase() !in setOf("http", "https") ||
            uri.host.isNullOrEmpty() ||
            uri.rawUserInfo != null ||
            uri.port !in -1..65535 ||
            uri.port == 0
        ) {
            throw ProtocolFailure(400, "INVALID_URL")
        }
        return value
    }
}
