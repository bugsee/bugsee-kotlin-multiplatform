package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal

public object Bugsee {
    private val bugseeInternal = BugseeInternal()


    public fun launch(apiKey: String) {
        bugseeInternal.launch(apiKey)
    }

    public fun launch(apiKey: String, options: Map<String, Any>) {
        bugseeInternal.launch(apiKey, options)
    }

    public fun launch(apiKey: String, options: BugseeLaunchOptions) {
        bugseeInternal.launch(apiKey, options)
    }

    public fun stop() {
        bugseeInternal.stop()
    }

    public fun relaunch() {
        bugseeInternal.relaunch()
    }

    public fun relaunch(options: BugseeLaunchOptions) {
        bugseeInternal.relaunch(options)
    }

    public fun relaunch(options: Map<String, Any>) {
        bugseeInternal.relaunch(options)
    }

    // Feedback methods
    public fun showFeedback() {
        bugseeInternal.showFeedback()
    }

    public fun setOnNewFeedbackListener(listener: BugseeFeedbackEventListener) {
        bugseeInternal.setOnNewFeedbackListener(listener)
    }

    public fun setDefaultFeedbackGreeting(greeting: String) {
        bugseeInternal.setDefaultFeedbackGreeting(greeting)
    }

    // Logging methods
    public fun log(message: String) {
        bugseeInternal.log(message)
    }

    public fun log(message: String, level: BugseeLogLevel) {
        bugseeInternal.log(message, level)
    }

    public fun trace(traceName: String, value: Any) {
        bugseeInternal.trace(traceName, value)
    }

    // Event methods
    public fun event(eventName: String) {
        bugseeInternal.event(eventName)
    }

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

    // Security methods
    public fun addSecureViewClass(className: String) {
        bugseeInternal.addSecureViewClass(className)
    }

    public fun removeSecureViewClass(className: String) {
        bugseeInternal.removeSecureViewClass(className)
    }

    // Lifecycle methods
    public fun pause() {
        bugseeInternal.pause()
    }

    public fun resume() {
        bugseeInternal.resume()
    }

    // Secure rectangle methods
    public fun addSecureRectangle(rect: BugseeSecureRectangle) {
        bugseeInternal.addSecureRectangle(rect)
    }

    public fun removeSecureRectangle(rect: BugseeSecureRectangle) {
        bugseeInternal.removeSecureRectangle(rect)
    }

    public fun removeAllSecureRectangles() {
        bugseeInternal.removeAllSecureRectangles()
    }

    public fun getAllSecureRectangles(): List<BugseeSecureRectangle> {
        return bugseeInternal.getAllSecureRectangles()
    }

    // Secure view methods
    public fun addSecureView(view: Any?) {
        bugseeInternal.addSecureView(view)
    }

    public fun addSecureWebView(view: Any?) {
        bugseeInternal.addSecureWebView(view)
    }

    public fun removeSecureView(view: Any?) {
        bugseeInternal.removeSecureView(view)
    }

    // Filter and listener methods
    public fun setNetworkEventFilter(filter: BugseeNetworkFilter?) {
        bugseeInternal.setNetworkEventFilter(filter)
    }

    public fun setLogFilter(filter: BugseeLogFilter?) {
        bugseeInternal.setLogFilter(filter)
    }

    public fun setLifecycleEventsListener(listener: BugseeLifecycleEventListener?) {
        bugseeInternal.setLifecycleEventsListener(listener)
    }

    // User management methods
    public fun setEmail(email: String) {
        bugseeInternal.setEmail(email)
    }

    public fun getEmail(): String? {
        return bugseeInternal.getEmail()
    }

    public fun clearEmail() {
        bugseeInternal.clearEmail()
    }

    // Attribute methods
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
    public fun createReport(provider: BugseeExtendedReportProvider) {
        bugseeInternal.createReport(provider)
    }

    public fun upload(report: BugseeExtendedReport) {
        bugseeInternal.upload(report)
    }

    // Report fields filter
    public fun setReportFieldsPreFilter(filler: BugseeReportFieldsFiller?) {
        bugseeInternal.setReportFieldsPreFilter(filler)
    }

    public fun setReportFieldsFilter(filter: BugseeReportFieldsFilter?) {
        bugseeInternal.setReportFieldsFilter(filter)
    }

    // View hierarchy capture
    public fun captureViewHierarchy() {
        bugseeInternal.captureViewHierarchy()
    }
}