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
     * this returns, and a JSON body is parsed on that thread. On iOS the network filter gets the
     * start of a request on the thread that starts it (the one that calls `resume` on its
     * `NSURLSessionTask`), so a filter that calls this parses a JSON request body there.
     */
    public fun sanitize(event: BugseeNetworkEvent)
}
