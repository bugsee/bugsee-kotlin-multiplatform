package com.bugsee.kmp

import kotlin.collections.set

public class BugseeExceptionLoggingOptions {
    public var exceptionDomain: String? = null
    public var includeVideo: Boolean = true
    public var labels: ArrayList<String>? = null
    public val rules: MergingRules = MergingRules()

    public class MergingRules {
        private val rules: HashMap<String, Any?> = HashMap()

        public var skipFrames: Int
            get() = rules["skipFrames"] as? Int ?: 0
            set(value) {
                rules["skipFrames"] = value
            }

        public fun setCustomOption(key: String, value: Any?) {
            rules[key] = value
        }

        public fun getCustomOption(key: String): Any? {
            return rules[key]
        }

        public fun toMap(): Map<String, Any?> {
            return rules
        }
    }
}