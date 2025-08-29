package com.bugsee.kmp

public expect class Bugsee() {

    public val appearance: BugseeAppearance

    // Execution control methods
    public fun launch(apiKey: String)

    public fun launch(apiKey: String, options: Map<String, Any>)

    public fun launch(apiKey: String, options: BugseeLaunchOptions)

    public fun stop()

    public fun relaunch()

    public fun relaunch(options: BugseeLaunchOptions)

    public fun relaunch(options: Map<String, Any>)

    // Feedback methods
    public fun showFeedback()

    public fun setOnNewFeedbackListener(listener: BugseeFeedbackEventListener)

    public fun setDefaultFeedbackGreeting(greeting: String)

    // Logging methods
    public fun log(message: String)

    public fun log(message: String, level: BugseeLogLevel)

    public fun trace(traceName: String, value: Any)

    // Event methods
    public fun event(eventName: String)

    public fun event(eventName: String, params: Map<String, Any>?)

    // Report dialog methods
    public fun showReportDialog()

    public fun showReportDialog(summary: String, description: String, severity: BugseeSeverity)

    public fun showReportDialog(summary: String, description: String, severity: BugseeSeverity, labels: List<String>?)

    // Upload methods
    public fun upload(summary: String, description: String, severity: BugseeSeverity)

    public fun upload(summary: String, description: String, severity: BugseeSeverity, labels: List<String>?)

    public fun upload(summary: String, description: String, severity: BugseeSeverity, labels: List<String>?, includeVideo: Boolean)

    // Exception logging methods
    public fun logException(ex: Throwable)

    public fun logException(ex: Throwable, options: BugseeExceptionLoggingOptions?)

    // Security methods
    public fun addSecureViewClass(className: String)

    public fun removeSecureViewClass(className: String)

    // Lifecycle methods
    public fun pause()

    public fun resume()

    // Secure rectangle methods
    public fun addSecureRectangle(rect: BugseeSecureRectangle)

    public fun removeSecureRectangle(rect: BugseeSecureRectangle)

    public fun removeAllSecureRectangles()

    public fun getAllSecureRectangles(): List<BugseeSecureRectangle>

    // Secure view methods
    public fun addSecureView(view: Any?)

    public fun addSecureWebView(view: Any?)

    public fun removeSecureView(view: Any?)

    // Filter and listener methods
    public fun setNetworkEventFilter(filter: BugseeNetworkFilter?)

    public fun setLogFilter(filter: BugseeLogFilter?)

    public fun setLifecycleEventsListener(listener: BugseeLifecycleEventListener?)

    // User management methods
    public fun setEmail(email: String)

    public fun getEmail(): String?

    public fun clearEmail()

    // Attribute methods
    public fun setAttribute(name: String, value: Any)

    public fun clearAttribute(name: String)

    public fun getAttribute(name: String): Any?

    public fun clearAllAttributes()

    // Report attachments provider
    public fun setReportAttachmentsProvider(provider: BugseeAttachmentsProvider?)

    // Data management
    public fun deleteCollectedDataOnDevice(deletionEventListener: EventHandler<Boolean>?)

    // Status methods
    public fun isLaunched(): Boolean

    // Extended report methods
    public fun createReport(provider: BugseeExtendedReportProvider)

    public fun upload(report: BugseeExtendedReport)

    // Report fields filter
    public fun setReportFieldsPreFiller(filler: BugseeReportFieldsFiller?)

    public fun setReportFieldsFilter(filter: BugseeReportFieldsFilter?)

    // View hierarchy capture
    public fun captureViewHierarchy()
}