package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal

public object Bugsee {
    private val bugseeInternal = BugseeInternal()

    public fun launch(apiKey: String) {
        bugseeInternal.launch(apiKey)
    }

    public fun launch(apiKey: String, options: Map<String, Any>?) {
        bugseeInternal.launch(apiKey, options)
    }

    public fun launch(apiKey: String, options: BugseeLaunchOptions?) {
        bugseeInternal.launch(apiKey, options)
    }

    public fun stop() {
        bugseeInternal.stop()
    }

    public fun relaunch() {
        bugseeInternal.relaunch()
    }

    public fun relaunch(options: BugseeLaunchOptions?) {
        bugseeInternal.relaunch(options)
    }

    public fun relaunch(options: Map<String, Any>?) {
        bugseeInternal.relaunch(options)
    }

    // Logging methods
    public fun log(message: String) {
        bugseeInternal.log(message)
    }

    public fun log(message: String, level: BugseeLogLevel) {
        bugseeInternal.log(message, level)
    }

    /**
     * Records [value] as the trace [traceName]. The value is a String, a Boolean, a number, or an
     * array, a collection or a map with String keys of such values, at any depth; arrays and
     * collections are recorded as lists. Any other value, and a map key that is not a String, is
     * recorded as its `toString()`, taken during the call. Arrays and collections are copied during
     * the call, so changing them later does not change the trace.
     *
     * A date is recorded as an ISO 8601 string in UTC, such as `2026-01-31T09:30:00.250Z`: a
     * `kotlin.time.Instant` as its `toString()`, a `java.util.Date` on Android and an `NSDate` on iOS
     * to the millisecond. On Android a `BigDecimal` or a `BigInteger` is recorded as an exact number.
     * On iOS `NSData`, `NSURL` and `NSNull` are recorded as the SDK writes them, and the SDK records a
     * Boolean as `1` or `0`.
     */
    public fun trace(traceName: String, value: Any) {
        bugseeInternal.trace(traceName, value)
    }

    // Event methods
    public fun event(eventName: String) {
        bugseeInternal.event(eventName)
    }

    /** Records the event [eventName] with [params], whose values are recorded as [trace] records a value. */
    public fun event(eventName: String, params: Map<String, Any>?) {
        bugseeInternal.event(eventName, params)
    }

    // Report dialog methods
    public fun showReportDialog() {
        bugseeInternal.showReportDialog()
    }

    public fun showReportDialog(summary: String, description: String, severity: BugseeSeverity) {
        bugseeInternal.showReportDialog(summary, description, severity)
    }

    public fun showReportDialog(summary: String, description: String, severity: BugseeSeverity, labels: List<String>?) {
        bugseeInternal.showReportDialog(summary, description, severity, labels)
    }

    // Upload methods
    public fun upload(summary: String,
                      description: String,
                      severity: BugseeSeverity) {
        bugseeInternal.upload(summary, description, severity)
    }

    public fun upload(summary: String,
                      description: String,
                      severity: BugseeSeverity,
                      labels: List<String>?) {
        bugseeInternal.upload(summary, description, severity, labels)
    }

    public fun upload(summary: String,
                      description: String,
                      severity: BugseeSeverity,
                      labels: List<String>?,
                      includeVideo: Boolean) {
        bugseeInternal.upload(summary, description, severity, labels, includeVideo)
    }

    // Exception logging methods
    public fun logException(ex: Throwable) {
        bugseeInternal.logException(ex)
    }

    public fun logException(ex: Throwable, options: BugseeExceptionLoggingOptions?) {
        bugseeInternal.logException(ex, options)
    }

    // Lifecycle methods
    public fun pause() {
        bugseeInternal.pause()
    }

    public fun resume() {
        bugseeInternal.resume()
    }

    // Secure view methods
    public fun addSecureView(view: Any?) {
        bugseeInternal.addSecureView(view)
    }

    public fun removeSecureView(view: Any?) {
        bugseeInternal.removeSecureView(view)
    }

    // Filter and listener methods

    /**
     * Sets the filter of the network events Bugsee captures, replacing the previous one; `null`
     * removes it. Return the event, changed or not, to keep it, or `null` to drop it. If the filter
     * throws, the event is dropped, so nothing it was meant to redact is recorded, and the first
     * failure of this filter is written once to the Bugsee log and the console.
     *
     * **A network filter turns off the SDK's built-in redaction of network events**
     * ([BugseeLaunchOptions.captureNetworkUseDefaultSanitizer]). While a filter is set, the
     * `Authorization` and `Cookie` headers, `token` query values and `password` body fields are
     * recorded as they are, unless the filter redacts them. To keep the built-in redaction, run
     * [defaultNetworkSanitizer] in the filter, then your own changes:
     * ```
     * Bugsee.setNetworkEventFilter { event ->
     *     event?.let { Bugsee.defaultNetworkSanitizer.sanitize(it) }
     *     // your own redaction
     *     event
     * }
     * ```
     */
    public fun setNetworkEventFilter(filter: BugseeNetworkFilter?) {
        bugseeInternal.setNetworkEventFilter(filter)
    }

    /**
     * The SDK's built-in network sanitizer: the redaction of captured network events while no
     * network filter is set and [BugseeLaunchOptions.captureNetworkUseDefaultSanitizer] is on. It
     * redacts the values of known sensitive keys in the URL query, in headers (`Authorization`,
     * `Cookie` and the like) and in JSON bodies. On Android it also removes `user:password@` from
     * the URL and redacts the error texts.
     *
     * Call it from the network filter to keep that redaction (see [setNetworkEventFilter]). The
     * sanitizer does not depend on the SDK state, so it works before [launch] and after [stop].
     */
    public val defaultNetworkSanitizer: BugseeNetworkSanitizer
        get() = bugseeInternal.defaultNetworkSanitizer

    /**
     * Sets the filter of the console lines Bugsee captures (logcat on Android, stdout and stderr on
     * iOS), replacing the previous one; `null` removes it. Return the event, changed or not, to keep
     * the line, or `null` to drop it. Lines logged with [log] are not filtered.
     *
     * Don't write to the console from the filter (`println`, `NSLog`, `android.util.Log`): Bugsee
     * captures that line too and passes it to the filter again. If the filter throws, the line is
     * dropped, and the first failure of this filter is written once to the Bugsee log and the
     * console.
     */
    public fun setLogFilter(filter: BugseeLogFilter?) {
        bugseeInternal.setLogFilter(filter)
    }

    public fun setLifecycleEventsListener(listener: BugseeLifecycleEventListener?) {
        bugseeInternal.setLifecycleEventsListener(listener)
    }

    // User identification

    /**
     * Sets the user identifier attached to every report from now on. The native SDK keeps it
     * across app restarts. If it is an email address, the server may link the reports to that
     * user's account. `null` or an empty string clears it, as [clearUserIdentifier] does.
     *
     * The email field of the report dialog edits the same value: what the user enters there
     * replaces the identifier, and clearing the field clears it.
     */
    public fun setUserIdentifier(userIdentifier: String?) {
        bugseeInternal.setUserIdentifier(userIdentifier)
    }

    /**
     * Returns the current user identifier: the last one set with [setUserIdentifier] or entered
     * in the report dialog's email field, or `null` if none is set.
     */
    public fun getUserIdentifier(): String? {
        return bugseeInternal.getUserIdentifier()
    }

    /** Clears the user identifier, so later reports are not associated with any user. */
    public fun clearUserIdentifier() {
        bugseeInternal.clearUserIdentifier()
    }

    // Attribute methods
    /**
     * Sets the attribute [name] to [value], a String, a Boolean or a number. A value of any other type,
     * including a collection, is not set, and an error is logged.
     *
     * Byte, Short, UByte and UShort values are stored as Int, UInt and ULong values as Long; a ULong
     * above `Long.MAX_VALUE` is not set. On Android the SDK stores a Double as a Float. On iOS
     * [getAttribute] returns a Boolean as a Byte, `1` or `0`.
     */
    public fun setAttribute(name: String, value: Any) {
        bugseeInternal.setAttribute(name, value)
    }

    public fun clearAttribute(name: String) {
        bugseeInternal.clearAttribute(name)
    }

    public fun getAttribute(name: String): Any? {
        return bugseeInternal.getAttribute(name)
    }

    public fun clearAllAttributes() {
        bugseeInternal.clearAllAttributes()
    }

    // Report attachments provider
    public fun setReportAttachmentsProvider(provider: BugseeAttachmentsProvider?) {
        bugseeInternal.setReportAttachmentsProvider(provider)
    }

    // Data management
    public fun deleteCollectedDataOnDevice(deletionEventListener: EventHandler<Boolean>?) {
        bugseeInternal.deleteCollectedDataOnDevice(deletionEventListener)
    }

    // Status methods
    public fun isLaunched(): Boolean {
        return bugseeInternal.isLaunched()
    }

    // Extended report methods
    /**
     * Creates a report from the current session and passes it to [provider], which can change it
     * and send it with [upload]. On Android the callbacks run on the main thread, on iOS on a
     * background thread.
     *
     * The SDK creates no report while Bugsee is not launched (on iOS also while it is launching),
     * or when it cannot prepare one: then [provider] is not called and a warning is logged. Use the
     * overload with `onNotCreated` or [createReportOrNull] to learn about it.
     */
    public fun createReport(provider: BugseeExtendedReportProvider) {
        bugseeInternal.createReport(provider, null)
    }

    /**
     * Like [createReport], and calls [onNotCreated] instead of [provider] when the SDK creates no
     * report: while Bugsee is not launched (on iOS also while it is launching), or when it cannot
     * prepare one.
     */
    public fun createReport(provider: BugseeExtendedReportProvider, onNotCreated: () -> Unit) {
        bugseeInternal.createReport(provider, onNotCreated)
    }

    /**
     * Like [createReport] with a single callback: [provider] gets the report, or `null` when the SDK
     * creates none (while Bugsee is not launched, on iOS also while it is launching, or when it
     * cannot prepare one).
     */
    public fun createReportOrNull(provider: EventHandler<BugseeExtendedReport?>) {
        bugseeInternal.createReport(provider = { provider(it) }, onNotCreated = { provider(null) })
    }

    public fun upload(report: BugseeExtendedReport) {
        bugseeInternal.upload(report)
    }

    // Report fields filter
    public fun setReportFieldsPreFilter(filler: BugseeReportFieldsFiller?) {
        bugseeInternal.setReportFieldsPreFilter(filler)
    }

    public fun setReportFieldsPostFilter(filter: BugseeReportFieldsFilter?) {
        bugseeInternal.setReportFieldsPostFilter(filter)
    }

    // View hierarchy capture
    public fun captureViewHierarchy() {
        bugseeInternal.captureViewHierarchy()
    }

    // Appearance access
    public val appearance: BugseeAppearance
        get() = bugseeInternal.appearance
}