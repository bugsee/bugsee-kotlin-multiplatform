package com.bugsee.kmp.internal

import swiftPMImport.com.bugsee.library.BugseeOptions
import swiftPMImport.com.bugsee.library.BugseeTheme
import com.bugsee.kmp.BugseeAppearance
import com.bugsee.kmp.BugseeAttachmentsProvider
import com.bugsee.kmp.BugseeDelegateWrapper
import com.bugsee.kmp.BugseeReportHandlerWrapper
import com.bugsee.kmp.BugseeExceptionLoggingOptions
import com.bugsee.kmp.BugseeExtendedReport
import com.bugsee.kmp.BugseeExtendedReportProvider
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

private typealias BugseeSDK = swiftPMImport.com.bugsee.library.Bugsee

public typealias ExtendedReport = swiftPMImport.com.bugsee.library.BugseeExtendedReport
public typealias LogLevel = swiftPMImport.com.bugsee.library.BugseeLogLevel

private const val TAG = "BugseeInternal"

public actual class BugseeInternal {
    public var logFilterHandler: BugseeLogFilter? = null
    public var lifecycleEventHandler: BugseeLifecycleEventListener? = null

    // The report fields filters and the attachments provider run on the SDK's report handler,
    // which is installed while any of them is set.
    public var attachmentsProviderHandler: BugseeAttachmentsProvider? = null
        set(value) {
            field = value
            synchronizeReportHandler()
        }
    public var reportFieldsFiller: BugseeReportFieldsFiller? = null
        set(value) {
            field = value
            synchronizeReportHandler()
        }
    public var reportFieldsFilter: BugseeReportFieldsFilter? = null
        set(value) {
            field = value
            synchronizeReportHandler()
        }

    internal val bugseeDelegate = BugseeDelegateWrapper(this)
    private val reportHandler = BugseeReportHandlerWrapper(this)

    private fun synchronizeReportHandler() {
        val needed = attachmentsProviderHandler != null || reportFieldsFiller != null || reportFieldsFilter != null
        BugseeSDK.setReportHandler(if (needed) reportHandler else null)
    }

    // +sharedInstance is declared nullable but is a dispatch_once singleton that exists before
    // launch, and launchWithToken / relaunch reuse it, so the delegate set here stays attached.
    init {
        val bugsee = BugseeSDK.sharedInstance()
        bugsee?.setDelegate(bugseeDelegate)
    }

    public actual val appearance: BugseeAppearance by lazy { BugseeAppearance() }

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
        // Decided from the KMP option, which also covers launch(apiKey, null).
        val installExceptionHook = shouldInstallUnhandledExceptionHook(options)
        BugseeSDK.launchWithToken(apiKey, nativeOptions) { started ->
            // set Bugsee exception hook only after successful launch
            if (started && installExceptionHook) {
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
        @Suppress("UNCHECKED_CAST")
        BugseeSDK.relaunchWithDictionaryOptions(options?.toMap() as? Map<Any?, *>)
    }

    // Like launch: a raw map goes through BugseeLaunchOptions.
    public actual fun relaunch(options: Map<String, Any>?) {
        relaunch(options?.let { BugseeLaunchOptions(it) })
    }

    public fun getAppearance(): BugseeTheme {
        return BugseeSDK.getAppearance()
    }

    // Logging methods
    public actual fun log(message: String) {
        BugseeSDK.log(message)
    }

    public actual fun log(message: String, level: BugseeLogLevel) {
        BugseeSDK.log(message, level.getLevelLong())
    }

    public actual fun trace(traceName: String, value: Any) {
        BugseeSDK.trace(traceName, value)
    }

    // Event methods
    public actual fun event(eventName: String) {
        BugseeSDK.event(eventName)
    }

    public actual fun event(eventName: String, params: Map<String, Any>?) {
        @Suppress("UNCHECKED_CAST")
        BugseeSDK.event(eventName, params as Map<Any?, *>?)
    }

    // Report dialog methods
    public actual fun showReportDialog() {
        BugseeSDK.showReportDialog()
    }

    public actual fun showReportDialog(summary: String, description: String, severity: BugseeSeverity) {
        BugseeSDK.showReportDialogWithSummary(summary, description, severity.getLevelLong())
    }

    public actual fun showReportDialog(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?
    ) {
        BugseeSDK.showReportDialogWithSummary(
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

    // iOS SDK 7 reports always include video; includeVideo = false cannot be honored.
    public actual fun upload(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?,
        includeVideo: Boolean
    ) {
        if (!includeVideo) {
            Logger.d(TAG, "upload: includeVideo = false is not supported by the iOS SDK — video is included")
        }

        upload(summary, description, severity, labels)
    }

    // Exception logging methods
    public actual fun logException(ex: Throwable) {
        BugseeSDK.logException(BugseeNSException(ex), null)
    }

    // iOS SDK 7 reads only the domain and skipFrames, as on Android; labels, includeVideo and
    // custom merging rules have no equivalent (labels can be added to the report in a report handler).
    public actual fun logException(ex: Throwable, options: BugseeExceptionLoggingOptions?) {
        if (options == null) {
            logException(ex)
            return
        }

        if (options.labels != null || !options.includeVideo) {
            Logger.d(TAG, "logException: labels and includeVideo are not supported by the iOS SDK — ignored")
        }
        BugseeSDK.logException(BugseeNSException(ex), options.exceptionDomain, options.rules.skipFrames.toLong(), null)
    }

    // Privacy control methods
    public actual fun pause() {
        BugseeSDK.startBlackout()
    }

    public actual fun resume() {
        BugseeSDK.endBlackout()
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
            var decision: swiftPMImport.com.bugsee.library.BugseeNetworkEvent? = nativeEvent
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

    // User identification
    public actual fun setUserIdentifier(userIdentifier: String?) {
        val value = UserIdentifierPolicy.forSet(userIdentifier)
        if (value == null) {
            BugseeSDK.clearUserIdentifier()
        } else {
            BugseeSDK.setUserIdentifier(value)
        }
    }

    public actual fun getUserIdentifier(): String? {
        return UserIdentifierPolicy.forGet(BugseeSDK.getUserIdentifier())
    }

    public actual fun clearUserIdentifier() {
        BugseeSDK.clearUserIdentifier()
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
            BugseeSDK.addSecureView(view)
        } else {
            Logger.e(
                "BugseeInternal",
                "addSecureView: expected platform.UIKit.UIView on iOS, got ${view?.let { it::class.simpleName } ?: "null"} — ignoring"
            )
        }
    }

    public actual fun removeSecureView(view: Any?) {
        if (view is UIView) {
            BugseeSDK.removeSecureView(view)
        } else {
            Logger.e(
                "BugseeInternal",
                "removeSecureView: expected platform.UIKit.UIView on iOS, got ${view?.let { it::class.simpleName } ?: "null"} — ignoring"
            )
        }
    }

    public actual fun isLaunched(): Boolean {
        return BugseeSDK.sharedInstance()?.launched ?: false
    }
}

/** The KMP unhandled-exception hook follows [BugseeLaunchOptions.detectAndReportCrash] (default on). */
internal fun shouldInstallUnhandledExceptionHook(options: BugseeLaunchOptions?): Boolean =
    options?.detectAndReportCrash ?: true
