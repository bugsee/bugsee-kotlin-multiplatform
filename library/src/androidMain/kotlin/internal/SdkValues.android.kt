package com.bugsee.kmp.internal

import java.math.BigDecimal
import java.math.BigInteger
import java.util.Date

// BigDecimal and BigInteger are immutable, and the SDK writes them as exact numbers. A Date (or a
// subclass, such as java.sql.Timestamp) is passed as an ISO 8601 string in UTC, as on iOS: the SDK
// would write its toString(), in the device time zone.
internal actual fun platformValue(value: Any): Any? = when (value) {
    is BigDecimal, is BigInteger -> value
    is Date -> isoDate(value.time)
    else -> null
}
