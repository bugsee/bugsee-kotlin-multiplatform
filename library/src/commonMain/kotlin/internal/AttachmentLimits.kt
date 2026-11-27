package com.bugsee.kmp.internal

/**
 * The largest attachment the native SDKs keep, in bytes: 3 MB (bugsee/specs#86). iOS:
 * `BGSReportMaxAttachmentBytes` (bugsee/bugsee-cocoa#354); Android: `IssueReport.MAX_ATTACHMENT_BYTES`
 * (bugsee/bugsee-android#867). The wrapper checks it itself, so that it does not read or copy an
 * attachment the SDK would drop.
 */
internal const val MAX_ATTACHMENT_BYTES: Long = 3L * 1024 * 1024

/**
 * Returns whether an attachment of [size] bytes is within [MAX_ATTACHMENT_BYTES], and logs a warning
 * when it is not.
 */
internal fun fitsAttachmentSizeLimit(tag: String, name: String, size: Long): Boolean {
    if (size <= MAX_ATTACHMENT_BYTES) return true
    Logger.w(tag, "attachment '$name' is $size bytes, over the limit of $MAX_ATTACHMENT_BYTES bytes, so it is not added")
    return false
}
