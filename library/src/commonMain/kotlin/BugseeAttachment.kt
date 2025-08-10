package com.bugsee.kmp

public class BugseeAttachment private constructor(
    public val name: String,
    public val filePath: String?,
    public val data: ByteArray?
) {
    public companion object {
        public fun create(name: String, data: ByteArray): BugseeAttachment {
            return BugseeAttachment(name, null, data)
        }

        public fun create(name: String, filePath: String): BugseeAttachment {
            return BugseeAttachment(name, filePath, null)
        }
    }
}