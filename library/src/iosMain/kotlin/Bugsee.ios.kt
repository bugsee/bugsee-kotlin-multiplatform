package com.bugsee.kmp

import cocoapods.Bugsee.BugseeTheme
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSException
import platform.UIKit.UIWebView
import platform.WebKit.WKWebView

private typealias BugseeSDK = cocoapods.Bugsee.Bugsee

public typealias ExtendedReport = cocoapods.Bugsee.BugseeExtendedReport
public typealias LaunchOptions = cocoapods.Bugsee.BugseeOptions
public typealias LogLevel = cocoapods.Bugsee.BugseeLogLevel
public typealias ExceptionLoggingOptions = cocoapods.Bugsee.BugseeExceptionLoggingOptions

public actual class Bugsee {
    private val bugseeDelegate = BugseeDelegateWrapper()

    init {
        val bugsee = BugseeSDK.sharedInstance()
        bugsee?.setDelegate(bugseeDelegate)
    }

    public actual fun launch(apiKey: String) {
        BugseeSDK.launchWithToken(apiKey)
    }

    public actual fun launch(apiKey: String, options: Map<Any?, Any?>) {
        BugseeSDK.launchWithToken(apiKey, options)
    }

    // Feedback methods
    public fun showFeedback() {
        BugseeSDK.showFeedbackController()
    }

    public fun setOnNewFeedbackListener(listener: BugseeFeedbackEventListener) {
        BugseeSDK //.setOnNewFeedbackListener(listener)
        // TODO: Implement through delegate wrapper!
    }

    public fun setDefaultFeedbackGreeting(greeting: String) {
        BugseeSDK.setDefaultFeedbackGreeting(greeting)
    }

    public fun getAppearance(): BugseeTheme {
        return BugseeSDK.appearance()
    }

    // Logging methods
    public fun log(message: String) {
        BugseeSDK.log(message)
    }

    public fun log(message: String, level: BugseeLogLevel) {
        BugseeSDK.log(message, level.getLevelLong())
    }

    public fun trace(traceName: String, value: Any) {
        BugseeSDK.traceKey(traceName, value)
    }

    // Event methods
    public fun event(eventName: String) {
        BugseeSDK.registerEvent(eventName)
    }

    public fun event(eventName: String, params: Map<Any?, Any>) {
        BugseeSDK.registerEvent(eventName, params)
    }

    // Report dialog methods
    public fun showReportDialog() {
        BugseeSDK.showReportController()
    }

    public fun showReportDialog(summary: String, description: String, severity: BugseeSeverity) {
        BugseeSDK.showReportControllerWithSummary(summary, description, severity.getLevelLong())
    }

    public fun showReportDialog(
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

    public fun upload(summary: String, description: String, severity: BugseeSeverity) {
        BugseeSDK.uploadWithSummary(summary, description, severity.getLevelLong())
    }

    public fun upload(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>
    ) {
        BugseeSDK.uploadWithSummary(summary, description, severity.getLevelLong(), labels)
    }

    public fun upload(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>,
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
    public fun logException(ex: NSException, options: ExceptionLoggingOptions?) {
        BugseeSDK.logException(ex, options, null)
    }

    public fun logException(
        ex: NSException,
        options: ExceptionLoggingOptions?,
        completion: (() -> Unit)?
    ) {
        BugseeSDK.logException(ex, options, completion)
    }

    // Lifecycle methods
    public fun pause() {
        BugseeSDK.pause()
    }

    public fun resume() {
        BugseeSDK.resume()
    }

    // Secure rectangle methods
    public fun addSecureRectangle(pixelRect: BugseeSecureRectangle) {
        val cgRect = CGRectMake(
            pixelRect.x,
            pixelRect.y,
            pixelRect.width,
            pixelRect.height
        )
        BugseeSDK.addSecureRect(cgRect)
    }

    public fun removeSecureRectangle(pixelRect: BugseeSecureRectangle) {
        val cgRect = CGRectMake(
            pixelRect.x,
            pixelRect.y,
            pixelRect.width,
            pixelRect.height
        )
        BugseeSDK.removeSecureRect(cgRect)
    }

    public fun removeAllSecureRectangles() {
        BugseeSDK.removeAllSecureRects()
    }

    public fun getAllSecureRectangles(): List<BugseeSecureRectangle> {
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
    public fun addSecureWebView(view: UIWebView) {
        BugseeSDK.addSecureWebView(view)
    }

    public fun addSecureWebView(view: WKWebView) {
        BugseeSDK.addSecureWebView(view)
    }

    // Filter and listener methods
    public fun setNetworkEventFilter(filter: NetworkEventFilter) {
        // TODO: Implement through delegate wrapper!
    }

    public fun setLogFilter(filter: LogFilter) {
        // TODO: Implement through delegate wrapper!
    }

    public fun setLifecycleEventsListener(listener: LifecycleEventListener) {
        // TODO: Implement through delegate wrapper!
    }

    // User management methods
    public fun setEmail(email: String) {
        BugseeSDK.setEmail(email)
    }

    public fun getEmail(): String? {
        return BugseeSDK.getEmail()
    }

    // Attribute methods
    public fun setAttribute(name: String, value: Any) {
        BugseeSDK.setAttribute(name, value)
    }

    public fun clearAttribute(name: String) {
        BugseeSDK.clearAttribute(name)
    }

    public fun getAttribute(name: String): Any? {
        return BugseeSDK.getAttribute(name)
    }

    public fun clearAllAttributes() {
        BugseeSDK.clearAllAttributes()
    }

    // Report attachments provider
    public fun setReportAttachmentsProvider(provider: ReportAttachmentsProvider) {
        // TODO: Implement through delegate wrapper!
    }

    // Control methods
    public fun stop() {
        BugseeSDK.stop({})
    }

    public fun deleteCollectedDataOnDevice(deletionEventListener: CompletionHandler<Boolean>?) {
        BugseeSDK.deleteCollectedDataOnDevice(deletionEventListener)
    }

    public fun relaunch() {
        BugseeSDK.relaunchWithOptions(null)
    }

    public fun relaunch(options: LaunchOptions) {
        BugseeSDK.relaunchWithOptions(options)
    }

    public fun getDeviceId(): String? {
        return BugseeSDK.getDeviceId()
    }

    // Extended report methods
    public fun createReport(listener: CompletionHandler<ExtendedReport?>?) {
        BugseeSDK.createReportWithCompletion(listener)
    }

    public fun upload(report: ExtendedReport) {
        BugseeSDK.uploadReport(report)
    }

    // Report fields filter
    public fun setReportFieldsFilter(filter: ReportFieldsFilter) {
        // TODO: Implement through delegate wrapper!
    }

    public fun captureViewHierarchy() {
        BugseeSDK.captureViewHierarchy()
    }
}