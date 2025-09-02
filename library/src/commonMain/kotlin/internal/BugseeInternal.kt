package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeAppearance
import com.bugsee.kmp.BugseeAttachmentsProvider
import com.bugsee.kmp.BugseeExceptionLoggingOptions
import com.bugsee.kmp.BugseeExtendedReport
import com.bugsee.kmp.BugseeExtendedReportProvider
import com.bugsee.kmp.BugseeFeedbackEventListener
import com.bugsee.kmp.BugseeLaunchOptions
import com.bugsee.kmp.BugseeLifecycleEventListener
import com.bugsee.kmp.BugseeLogFilter
import com.bugsee.kmp.BugseeLogLevel
import com.bugsee.kmp.BugseeNetworkFilter
import com.bugsee.kmp.BugseeReportFieldsFiller
import com.bugsee.kmp.BugseeReportFieldsFilter
import com.bugsee.kmp.BugseeSecureRectangle
import com.bugsee.kmp.BugseeSeverity
import com.bugsee.kmp.EventHandler

public expect class BugseeInternal() {

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
    public fun setReportFieldsPreFilter(filler: BugseeReportFieldsFiller?)

    public fun setReportFieldsFilter(filter: BugseeReportFieldsFilter?)

    // View hierarchy capture
    public fun captureViewHierarchy()
}