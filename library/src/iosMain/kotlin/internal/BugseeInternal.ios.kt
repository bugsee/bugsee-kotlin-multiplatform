package com.bugsee.kmp.internal

import cocoapods.Bugsee.BugseeOptions
import cocoapods.Bugsee.BugseeTheme
import com.bugsee.kmp.BugseeAppearance
import com.bugsee.kmp.BugseeAttachmentsProvider
import com.bugsee.kmp.BugseeDelegateWrapper
import com.bugsee.kmp.BugseeExceptionLoggingOptions
import com.bugsee.kmp.BugseeExtendedReport
import com.bugsee.kmp.BugseeExtendedReportProvider
import com.bugsee.kmp.BugseeFeedbackEventListener
import com.bugsee.kmp.BugseeIOSUtils
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
import com.bugsee.kmp.nsexception.*
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake

private typealias BugseeSDK = cocoapods.Bugsee.Bugsee

public typealias ExtendedReport = cocoapods.Bugsee.BugseeExtendedReport
public typealias LogLevel = cocoapods.Bugsee.BugseeLogLevel
public typealias ExceptionLoggingOptions = cocoapods.Bugsee.BugseeExceptionLoggingOptions

public actual class BugseeInternal {
    public actual var logFilterHandler: BugseeLogFilter? = null
    public actual var networkFilterHandler: BugseeNetworkFilter? = null
    private val bugseeDelegate = BugseeDelegateWrapper(this)

    init {
        val bugsee = BugseeSDK.sharedInstance()
        bugsee?.setDelegate(bugseeDelegate)
    }

    public actual val appearance: BugseeAppearance
        get() = TODO("Not yet implemented")

    // Execution control methods
    public actual fun launch(apiKey: String) {
        launch(apiKey, BugseeLaunchOptions())
    }

    public actual fun launch(apiKey: String, options: Map<String, Any>?) {
        var launchOptions: BugseeLaunchOptions? = null
        if (options != null) {
            launchOptions = BugseeLaunchOptions(options)
        }

        launch(apiKey, launchOptions)
    }

    public actual fun launch(apiKey: String, options: BugseeLaunchOptions?) {
        var nativeOptions: BugseeOptions? = null
        if (options != null) {
            @Suppress("UNCHECKED_CAST")
            nativeOptions = BugseeOptions.optionsFrom(options.toMap() as? Map<Any?, *>)
        }
        BugseeSDK.launchWithToken(apiKey, nativeOptions) { started ->
            // set Bugsee exception hook only after successful launch
            // and if crashReport option is set to true.
            // Note: the default value for crashReport is true.
            if (started && (nativeOptions?.crashReport ?: true)) {
                setBugseeUnhandledExceptionHook()
            }
        }
    }

    public actual fun stop() {
        BugseeSDK.stop({})
    }

    public actual fun relaunch() {
        BugseeSDK.relaunchWithOptions(null)
    }

    public actual fun relaunch(options: BugseeLaunchOptions?) {
        relaunch(HashMap(if (options != null) HashMap(options.toMap()) else HashMap()))
    }

    public actual fun relaunch(options: Map<String, Any>?) {
        @Suppress("UNCHECKED_CAST")
        BugseeSDK.relaunchWithDictionaryOptions(options as? Map<Any?, *>)
    }

    // Feedback methods
    public actual fun showFeedback() {
        BugseeSDK.showFeedbackController()
    }

    public actual fun setOnNewFeedbackListener(listener: BugseeFeedbackEventListener) {
        BugseeSDK //.setOnNewFeedbackListener(listener)
        // TODO: Implement through delegate wrapper!
    }

    public actual fun setDefaultFeedbackGreeting(greeting: String) {
        BugseeSDK.setDefaultFeedbackGreeting(greeting)
    }

    public fun getAppearance(): BugseeTheme {
        return BugseeSDK.appearance()
    }

    // Logging methods
    public actual fun log(message: String) {
        BugseeSDK.log(message)
    }

    public actual fun log(message: String, level: BugseeLogLevel) {
        BugseeSDK.log(message, level.getLevelLong())
    }

    public actual fun trace(traceName: String, value: Any) {
        BugseeSDK.traceKey(traceName, value)
    }

    // Event methods
    public actual fun event(eventName: String) {
        BugseeSDK.registerEvent(eventName)
    }

    public actual fun event(eventName: String, params: Map<String, Any>?) {
        @Suppress("UNCHECKED_CAST")
        BugseeSDK.registerEvent(eventName, params as Map<Any?, *>)
    }

    // Report dialog methods
    public actual fun showReportDialog() {
        BugseeSDK.showReportController()
    }

    public actual fun showReportDialog(summary: String, description: String, severity: BugseeSeverity) {
        BugseeSDK.showReportControllerWithSummary(summary, description, severity.getLevelLong())
    }

    public actual fun showReportDialog(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?
    ) {
        BugseeSDK.showReportControllerWithSummary(
            summary,
            description,
            severity.getLevelLong(),
            labels
        )
    }

    public actual fun upload(summary: String, description: String, severity: BugseeSeverity) {
        BugseeSDK.uploadWithSummary(summary, description, severity.getLevelLong())
    }

    public actual fun upload(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?
    ) {
        BugseeSDK.uploadWithSummary(summary, description, severity.getLevelLong(), labels)
    }

    public actual fun upload(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?,
        includeVideo: Boolean
    ) {
        BugseeSDK.uploadWithSummary(
            summary,
            description,
            severity.getLevelLong(),
            labels,
            includeVideo
        )
    }

    // Exception logging methods
    public actual fun logException(ex: Throwable) {
        logException(ex, null)
    }

    public actual fun logException(ex: Throwable, options: BugseeExceptionLoggingOptions?) {
        val exception = BugseeNSException(ex)
        val sdkExceptionLoggingOptions = BugseeIOSUtils.convertExceptionLoggingOptions(options)
        BugseeSDK.logException(exception, sdkExceptionLoggingOptions, null)
    }

    // Lifecycle methods
    public actual fun pause() {
        BugseeSDK.pause()
    }

    public actual fun resume() {
        BugseeSDK.resume()
    }

    // Secure rectangle methods
    public actual fun addSecureRectangle(rect: BugseeSecureRectangle) {
        val cgRect = CGRectMake(
            rect.x,
            rect.y,
            rect.width,
            rect.height
        )

        BugseeSDK.addSecureRect(cgRect)
    }

    public actual fun removeSecureRectangle(rect: BugseeSecureRectangle) {
        val cgRect = CGRectMake(
            rect.x,
            rect.y,
            rect.width,
            rect.height
        )

        BugseeSDK.removeSecureRect(cgRect)
    }

    public actual fun removeAllSecureRectangles() {
        BugseeSDK.removeAllSecureRects()
    }

    public actual fun getAllSecureRectangles(): List<BugseeSecureRectangle> {
        val rectsList = BugseeSDK.getAllSecureRects()
        if (rectsList == null) {
            return emptyList()
        }

        // TODO: Test the conversion below!
        val resultingRects = mutableListOf<BugseeSecureRectangle>()
        for (rect in rectsList) {
            if (rect is CGRect) {
                resultingRects.add(
                    BugseeSecureRectangle(
                        rect.origin.x,
                        rect.origin.y,
                        rect.size.width,
                        rect.size.height
                    )
                )
            }
        }

        return resultingRects
    }

    // Secure view methods
    public actual fun addSecureWebView(view: Any?) {
        // TODO: Test webview class
        BugseeSDK.addSecureWebView(view)
    }

//    public fun addSecureWebView(view: WKWebView) {
//        BugseeSDK.addSecureWebView(view)
//    }

    // Filter and listener methods
    public actual fun setNetworkEventFilter(filter: BugseeNetworkFilter?) {
        networkFilterHandler = filter
    }

    public actual fun setLogFilter(filter: BugseeLogFilter?) {
        logFilterHandler = filter
    }

    public actual fun setLifecycleEventsListener(listener: BugseeLifecycleEventListener?) {
        // TODO: Implement through delegate wrapper!
    }

    // User management methods
    public actual fun setEmail(email: String) {
        BugseeSDK.setEmail(email)
    }

    public actual fun getEmail(): String? {
        return BugseeSDK.getEmail()
    }

    public actual fun clearEmail() {
        BugseeSDK.clearEmail()
    }

    // Attribute methods
    public actual fun setAttribute(name: String, value: Any) {
        BugseeSDK.setAttribute(name, value)
    }

    public actual fun clearAttribute(name: String) {
        BugseeSDK.clearAttribute(name)
    }

    public actual fun getAttribute(name: String): Any? {
        return BugseeSDK.getAttribute(name)
    }

    public actual fun clearAllAttributes() {
        BugseeSDK.clearAllAttributes()
    }

    // Report attachments provider
    public actual fun setReportAttachmentsProvider(provider: BugseeAttachmentsProvider?) {
        // TODO: Implement through delegate wrapper
    }


    // Control methods
    public actual fun deleteCollectedDataOnDevice(deletionEventListener: EventHandler<Boolean>?) {
        BugseeSDK.deleteCollectedDataOnDevice(deletionEventListener)
    }

//    public fun getDeviceId(): String? {
//        return BugseeSDK.getDeviceId()
//    }

    // Extended report methods
    public actual fun createReport(provider: BugseeExtendedReportProvider) {
        // TODO
    }

    public fun upload(report: ExtendedReport) {
        BugseeSDK.uploadReport(report)
    }

    // Report fields filter
    public actual fun setReportFieldsPreFilter(filler: BugseeReportFieldsFiller?) {
        // TODO: Implement through delegate wrapper!
    }

    public actual fun setReportFieldsFilter(filter: BugseeReportFieldsFilter?) {
    }

    // View hierarchy capture
    public actual fun captureViewHierarchy() {
        BugseeSDK.captureViewHierarchy()
    }

    public actual fun upload(report: BugseeExtendedReport) {
        // Convert our type to the native type
        val nativeReport = report as? ExtendedReport
        if (nativeReport != null) {
            BugseeSDK.uploadReport(nativeReport)
        }
    }

    public actual fun addSecureViewClass(className: String) {
        // iOS doesn't have activity classes like Android
        // This method is a no-op for iOS
    }

    public actual fun removeSecureViewClass(className: String) {
        // iOS doesn't have activity classes like Android
        // This method is a no-op for iOS
    }

    public actual fun addSecureView(view: Any?) {
        // TODO: Implement view security for iOS
    }

    public actual fun removeSecureView(view: Any?) {
        // TODO: Implement view security for iOS
    }

    public actual fun isLaunched(): Boolean {
        // TODO: Do we need this API?
        return true
    }
}