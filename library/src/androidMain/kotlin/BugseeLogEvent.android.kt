package com.bugsee.kmp

import com.bugsee.library.logs.BugseeLog

public actual class BugseeLogEvent internal constructor(
        internal val underlyingEvent: BugseeLog
) {
        // Use underlying object ref to minimize allocations and copying
        public actual var message: String
                get() = underlyingEvent.message ?: ""
                set(value) {
                        underlyingEvent.message = value
                }

        public actual val level: BugseeLogLevel = BugseeAndroidUtils.convertLogLevel(underlyingEvent.level)
}
