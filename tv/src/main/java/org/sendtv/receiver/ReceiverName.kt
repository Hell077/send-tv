package org.sendtv.receiver

/** Validate owner-entered names without silently changing them. */
object ReceiverName {
    const val MAX_LENGTH = 48

    fun isValid(value: String): Boolean =
        value.isNotBlank() &&
            value.length <= MAX_LENGTH &&
            value == value.trim() &&
            value.none { it.isISOControl() || Character.getType(it) == Character.FORMAT.toInt() }
}
