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
import com.bugsee.kmp.BugseeSeverity
import com.bugsee.kmp.EventHandler
import com.bugsee.kmp.nsexception.*
import platform.UIKit.UIView

private typealias BugseeSDK = cocoapods.Bugsee.Bugsee

public typealias ExtendedReport = cocoapods.Bugsee.BugseeExtendedReport
public typealias LogLevel = cocoapods.Bugsee.BugseeLogLevel
public typealias ExceptionLoggingOptions = cocoapods.Bugsee.BugseeExceptionLoggingOptions

public actual class BugseeInternal {
    public var logFilterHandler: BugseeLogFilter? = null
    public var lifecycleEventHandler: BugseeLifecycleEventListener? = null
    public var attachmentsProviderHandler: BugseeAttachmentsProvider? = null
    public var reportFieldsFiller: BugseeReportFieldsFiller? = null
    public var reportFieldsFilter: BugseeReportFieldsFilter? = null
    public var feedbackHandler: BugseeFeedbackEventListener? = null
    private val bugseeDelegate = BugseeDelegateWrapper(this)

    init {
        val bugsee = BugseeSDK.sharedInstance()
        bugsee?.setDelegate(bugseeDelegate)
    }

    public actual val appearance: BugseeAppearance
        get() = BugseeAppearance()

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
        feedbackHandler = listener
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

    // Filter and listener methods
    public actual fun setNetworkEventFilter(filter: BugseeNetworkFilter?) {
        if (filter == null) {
            BugseeSDK.setNetworkEventFilter(null)
            return
        }
        BugseeSDK.setNetworkEventFilter { nativeEvent, completionHandler ->
            // Default to keeping the original event; flipped to filtered.impl
            // (or null to drop) if the filter runs successfully.
            var decision: cocoapods.Bugsee.BugseeNetworkEvent? = nativeEvent
            try {
                if (nativeEvent != null) {
                    val filtered = filter.invoke(com.bugsee.kmp.BugseeNetworkEvent(impl = nativeEvent))
                    // null => drop; non-null => keep (mutations in `impl` propagate)
                    decision = filtered?.impl
                }
            } catch (e: Throwable) {
                com.bugsee.kmp.Bugsee.log(
                    "BugseeInternal: exception during networkFilterHandler invoke: ${e.message}",
                    BugseeLogLevel.Warning
                )
                decision = nativeEvent
            }
            try {
                completionHandler?.invoke(decision)
            } catch (e: Throwable) {
                Logger.e("BugseeInternal", "networkFilter completionHandler threw: ${e.message}")
            }
        }
    }

    public actual fun setLogFilter(filter: BugseeLogFilter?) {
        logFilterHandler = filter
    }

    public actual fun setLifecycleEventsListener(listener: BugseeLifecycleEventListener?) {
        lifecycleEventHandler = listener
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
        attachmentsProviderHandler = provider
    }


    // Control methods
    public actual fun deleteCollectedDataOnDevice(deletionEventListener: EventHandler<Boolean>?) {
        BugseeSDK.deleteCollectedDataOnDevice { success ->
            try {
                deletionEventListener?.invoke(success)
            } catch (e: Throwable) {
                Logger.e("BugseeInternal", "deleteCollectedDataOnDevice: listener threw: ${e.message}")
            }
        }
    }

    // Extended report methods
    public actual fun createReport(provider: BugseeExtendedReportProvider) {
        BugseeSDK.createReportWithCompletion { nativeReport ->
            try {
                if (nativeReport != null) {
                    provider.invoke(BugseeExtendedReport(nativeReport))
                } else {
                    Logger.d("BugseeInternal", "createReport: native SDK returned null, provider not invoked")
                }
            } catch (e: Throwable) {
                Logger.e("BugseeInternal", "createReport: provider threw: ${e.message}")
            }
        }
    }

    // Report fields filter
    public actual fun setReportFieldsPreFilter(filler: BugseeReportFieldsFiller?) {
        reportFieldsFiller = filler
    }

    public actual fun setReportFieldsPostFilter(filter: BugseeReportFieldsFilter?) {
        reportFieldsFilter = filter
    }

    // View hierarchy capture
    public actual fun captureViewHierarchy() {
        BugseeSDK.captureViewHierarchy()
    }

    public actual fun upload(report: BugseeExtendedReport) {
        BugseeSDK.uploadReport(report.underlyingReport)
    }

    // Security methods
    // Secure view methods
    public actual fun addSecureView(view: Any?) {
        if (view is UIView) {
            BugseeSDK.setView(view, asHidden = true)
        }
    }

    public actual fun removeSecureView(view: Any?) {
        if (view is UIView) {
            BugseeSDK.setView(view, asHidden = false)
        }
    }

    public actual fun isLaunched(): Boolean {
        return BugseeSDK.sharedInstance()?.launched ?: false
    }
}
