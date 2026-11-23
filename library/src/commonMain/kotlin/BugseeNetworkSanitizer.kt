package com.bugsee.kmp

/**
 * Redacts sensitive data from a captured [BugseeNetworkEvent].
 *
 * The SDK's own one is [Bugsee.defaultNetworkSanitizer]. A network filter set with
 * [Bugsee.setNetworkEventFilter] turns off the SDK's built-in redaction, so a filter that wants
 * to keep it calls this sanitizer itself.
 */
public interface BugseeNetworkSanitizer {

    /**
     * Redacts [event] in place, synchronously on the calling thread: the event is changed before
     * this returns, and a JSON body is parsed on that thread.
     */
    public fun sanitize(event: BugseeNetworkEvent)
}
