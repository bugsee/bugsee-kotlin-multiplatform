package com.bugsee.kmp.internal

import android.view.View
import com.bugsee.kmp.*
import com.bugsee.library.contracts.common.Callback1
import com.bugsee.library.contracts.exchange.EventFilter
import com.bugsee.library.contracts.exchange.LogEvent
import com.bugsee.library.contracts.exchange.NetworkEvent
import com.bugsee.library.contracts.lifecycle.LifecycleEventListener
import com.bugsee.library.contracts.reporting.Report
import com.bugsee.library.contracts.reporting.ReportCreationListener
import com.bugsee.library.contracts.reporting.ReportHandler
import java.io.Serializable

private typealias BugseeSDK = com.bugsee.library.Bugsee

public actual class BugseeInternal {

    private companion object {
        private const val TAG = "BugseeInternal"
        private const val REPORT_NOT_CREATED =
            "createReport: the SDK created no report (Bugsee is not launched, or the report could not be " +
                "prepared); the provider is not called"
    }

    // Registered before any launch, so every session carries the wrapper metadata (sdk.wrapper),
    // also one started by launch(apiKey) or relaunch() without options. The SDK holds it strongly;
    // it is kept here too, as the instance this object registered.
    internal val wrapperAdapter = BugseeWrapperAdapter()

    init {
        BugseeSDK.setWrapper(wrapperAdapter)
    }

    // Read on the SDK's report-handler thread, written from the caller's thread.
    @Volatile
    private var reportFieldsFiller: BugseeReportFieldsFiller? = null
    @Volatile
    private var reportFieldsFilter: BugseeReportFieldsFilter? = null
    @Volatile
    private var attachmentsProvider: BugseeAttachmentsProvider? = null

    // The SDK appearance is a process-wide singleton; one wrapper keeps wrapper-side state
    // (notificationTitleResId) stable across accesses.
    public actual val appearance: BugseeAppearance by lazy { BugseeAppearance(BugseeSDK.getAppearance()) }

    // Launch methods
    // A raw map goes through BugseeLaunchOptions, as on iOS: defaults and type checks.
    public actual fun launch(apiKey: String, options: Map<String, Any>?) {
        launch(apiKey, options?.let { BugseeLaunchOptions(it) })
    }

    public actual fun launch(apiKey: String) {
        BugseeSDK.launch(apiKey)
    }

    public actual fun launch(apiKey: String, options: BugseeLaunchOptions?) {
        val context = applicationContext ?: run {
            Logger.d(TAG, "Bugsee KMP: cannot launch() — applicationContext is not gathered")
            return
        }

        BugseeSDK.launch(context, apiKey, BugseeAndroidUtils.toSerializableMap(options?.toMap(), "launch"))
    }

    public actual fun stop() {
        BugseeSDK.stop()
    }

    public actual fun relaunch() {
        BugseeSDK.relaunch()
    }

    public actual fun relaunch(options: BugseeLaunchOptions?) {
        BugseeSDK.relaunch(BugseeAndroidUtils.toSerializableMap(options?.toMap(), "relaunch"))
    }

    // Like launch: a raw map goes through BugseeLaunchOptions.
    public actual fun relaunch(options: Map<String, Any>?) {
        relaunch(options?.let { BugseeLaunchOptions(it) })
    }



    // Logging methods
    public actual fun log(message: String) {
        BugseeSDK.log(message)
    }

    public actual fun log(message: String, level: BugseeLogLevel) {
        BugseeSDK.log(
            message,
            BugseeAndroidUtils.convertLogLevel(level)
        )
    }

    public actual fun trace(traceName: String, value: Any) {
        BugseeSDK.trace(traceName, sdkValue(value))
    }


    // Event methods
    public actual fun event(eventName: String) {
        BugseeSDK.event(eventName)
    }

    public actual fun event(eventName: String, params: Map<String, Any>?) {
        if (params == null) {
            event(eventName)
            return
        }

        BugseeSDK.event(eventName, eventParams(params))
    }


    // Report dialog methods
    public actual fun showReportDialog() {
        BugseeSDK.showReportDialog()
    }

    public actual fun showReportDialog(
        summary: String,
        description: String,
        severity: BugseeSeverity
    ) {
        BugseeSDK.showReportDialog(
            summary,
            description,
            BugseeAndroidUtils.convertSeverity(severity)
        )
    }

    public actual fun showReportDialog(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?
    ) {
        if (labels == null) {
            showReportDialog(summary, description, severity)
            return
        }

        BugseeSDK.showReportDialog(
            summary,
            description,
            BugseeAndroidUtils.convertSeverity(severity),
            ArrayList(labels)
        )
    }


    // Bug report upload methods
    public actual fun upload(summary: String, description: String, severity: BugseeSeverity) {
        BugseeSDK.upload(summary, description, BugseeAndroidUtils.convertSeverity(severity))
    }

    public actual fun upload(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?
    ) {
        if (labels == null) {
            upload(summary, description, severity)
            return
        }

        BugseeSDK.upload(
            summary,
            description,
            BugseeAndroidUtils.convertSeverity(severity),
            ArrayList(labels)
        )
    }

    // 7.x reports always include video; includeVideo = false cannot be honored.
    public actual fun upload(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?,
        includeVideo: Boolean
    ) {
        if (!includeVideo) {
            Logger.d(TAG, "upload: includeVideo = false is not supported by the Android SDK — video is included")
        }

        upload(summary, description, severity, labels)
    }


    // Exception logging methods
    public actual fun logException(ex: Throwable) {
        BugseeSDK.logException(ex)
    }

    // SDK 7 reads only the domain and skipFrames; labels, includeVideo and custom merging rules
    // have no Android equivalent (labels can be added to the report in a report handler).
    public actual fun logException(ex: Throwable, options: BugseeExceptionLoggingOptions?) {
        if (options == null) {
            logException(ex)
            return
        }

        if (options.labels != null || !options.includeVideo) {
            Logger.d(TAG, "logException: labels and includeVideo are not supported by the Android SDK — ignored")
        }
        BugseeSDK.logException(ex, options.exceptionDomain, options.rules.skipFrames)
    }

    // Privacy control methods
    public actual fun pause() {
        BugseeSDK.startBlackout()
    }

    public actual fun resume() {
        BugseeSDK.endBlackout()
    }


    // Status checks
    public actual fun isLaunched(): Boolean {
        return BugseeSDK.getLaunched()
    }

    // Secure view methods
    public actual fun addSecureView(view: Any?) {
        if (view is View) {
            BugseeSDK.addSecureView(view)
        } else {
            Logger.e(
                TAG,
                "addSecureView: expected android.view.View on Android, got ${view?.let { it::class.simpleName } ?: "null"} — ignoring"
            )
        }
    }

    public actual fun removeSecureView(view: Any?) {
        if (view is View) {
            BugseeSDK.removeSecureView(view)
        } else {
            Logger.e(
                TAG,
                "removeSecureView: expected android.view.View on Android, got ${view?.let { it::class.simpleName } ?: "null"} — ignoring"
            )
        }
    }


    // Filter and listener methods
    // 7.x filters are callback-based: callback.run(event) keeps the (mutated) event,
    // callback.run(null) drops it. A filter that throws drops the event, as the SDK does, and
    // reports its first failure once (FilterFailureReporter).
    public actual fun setNetworkEventFilter(filter: BugseeNetworkFilter?) {
        if (filter == null) {
            BugseeSDK.setNetworkEventFilter(null)
            return
        }

        val failures = FilterFailureReporter(SdkFilter.Network)
        BugseeSDK.setNetworkEventFilter(object : EventFilter<NetworkEvent> {
            override fun filter(event: NetworkEvent, callback: Callback1<NetworkEvent>) {
                // Timing updates have no 0.1.x stage; don't let a filter written for
                // BugseeNetworkEventStage.Before drop them.
                if (event.networkEventType == NetworkEvent.NetworkEventStage.RequestTimingsReceived) {
                    callback.run(event)
                    return
                }

                // Throwable: an Error (TODO(), AssertionError) is not an Exception.
                val result = try {
                    filter.invoke(BugseeNetworkEvent(event))?.underlyingEvent
                } catch (e: Throwable) {
                    failures.onFailure(e)
                    null
                }
                callback.run(result)
            }
        })
    }

    public actual val defaultNetworkSanitizer: BugseeNetworkSanitizer = DefaultNetworkSanitizer

    public actual fun setLogFilter(filter: BugseeLogFilter?) {
        if (filter == null) {
            BugseeSDK.setLogEventFilter(null)
            return
        }

        // A filter that throws drops the line, as the SDK does. Each filter reports its first
        // failure once, and not through logcat from here: the SDK reads logcat back through this
        // filter (see FilterFailureReporter).
        val failures = FilterFailureReporter(SdkFilter.Log)
        BugseeSDK.setLogEventFilter(object : EventFilter<LogEvent> {
            override fun filter(event: LogEvent, callback: Callback1<LogEvent>) {
                val result = try {
                    filter.invoke(BugseeLogEvent(event))?.underlyingEvent
                } catch (e: Throwable) {
                    failures.onFailure(e)
                    null
                }
                callback.run(result)
            }
        })
    }

    public actual fun setLifecycleEventsListener(listener: BugseeLifecycleEventListener?) {
        if (listener == null) {
            BugseeSDK.setLifecycleEventsListener(null)
            return
        }

        BugseeSDK.setLifecycleEventsListener(object : LifecycleEventListener {
            override fun onEvent(event: String, payload: Any?) {
                val converted = BugseeAndroidUtils.convertLifecycleEvent(event) ?: return

                // Throwable: the SDK calls this on its own thread with no catch, so anything that
                // escapes crashes the app.
                try {
                    listener.invoke(converted)
                } catch (e: Throwable) {
                    Logger.e(TAG, "exception during setLifecycleEventsListener execution", e)
                }
            }
        })
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
    // attributeValue returns a String or a boxed number or Boolean, all Serializable.
    public actual fun setAttribute(name: String, value: Any) {
        val sdkValue = attributeValue(name, value) ?: return
        BugseeSDK.setAttribute(name, sdkValue as Serializable)
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
    @Synchronized
    public actual fun setReportAttachmentsProvider(provider: BugseeAttachmentsProvider?) {
        attachmentsProvider = provider
        synchronizeReportHandler()
    }


    // Data management
    // 6.x deleted everything collected; deleting intermediate capture data and unsent report
    // bundles keeps that behavior.
    public actual fun deleteCollectedDataOnDevice(deletionEventListener: EventHandler<Boolean>?) {
        BugseeSDK.deleteCollectedDataOnDevice(
            true,
            true,
            deletionEventListener?.let { listener ->
                Callback1<Boolean> {
                    try {
                        listener.invoke(it == true)
                    } catch (e: Throwable) {
                        Logger.e(TAG, "deleteCollectedDataOnDevice: the listener threw", e)
                    }
                }
            }
        )
    }


    // Extended report methods
    public actual fun createReport(provider: BugseeExtendedReportProvider, onNotCreated: (() -> Unit)?) {
        // The SDK calls this on the main thread with no catch; a throw is logged, as on iOS.
        BugseeSDK.createReport(ReportCreationListener { report ->
            try {
                if (report == null) {
                    Logger.w(TAG, REPORT_NOT_CREATED)
                    onNotCreated?.invoke()
                } else {
                    provider.invoke(BugseeExtendedReport(report))
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "createReport: a callback threw", e)
            }
        })
    }

    public actual fun upload(report: BugseeExtendedReport) {
        BugseeSDK.upload(report.underlyingReport)
    }


    // Report fields filter
    @Synchronized
    public actual fun setReportFieldsPreFilter(filler: BugseeReportFieldsFiller?) {
        reportFieldsFiller = filler
        synchronizeReportHandler()
    }

    @Synchronized
    public actual fun setReportFieldsPostFilter(filter: BugseeReportFieldsFilter?) {
        reportFieldsFilter = filter
        synchronizeReportHandler()
    }

    // 7.x folds the 6.x report fields filter and attachments provider into a single
    // ReportHandler: the pre-filter runs before the report is created, the post-filter
    // and the attachments provider after. The completion callback is always invoked.
    private fun synchronizeReportHandler() {
        if (reportFieldsFiller == null && reportFieldsFilter == null && attachmentsProvider == null) {
            BugseeSDK.setReportHandler(null)
            return
        }

        BugseeSDK.setReportHandler(object : ReportHandler {
            override fun onBeforeReportCreated(report: Report, isTerminating: Boolean, completionCallback: Runnable) {
                try {
                    reportFieldsFiller?.let { filler ->
                        val original = BugseeAndroidUtils.convertReportFieldsFromNative(report)
                        val fields = BugseeAndroidUtils.convertReportFieldsFromNative(report)
                        filler.invoke(fields)
                        BugseeAndroidUtils.applyReportFieldsToNative(fields, original, report)
                    }
                } catch (e: Throwable) {
                    Logger.e(TAG, "exception during report fields pre-filter execution", e)
                } finally {
                    completionCallback.run()
                }
            }

            override fun onAfterReportCreated(report: Report, isTerminating: Boolean, completionCallback: Runnable) {
                // The post-filter and the attachments provider were independent hooks in 6.x:
                // a failure in one must not cancel the other.
                try {
                    try {
                        reportFieldsFilter?.let { filter ->
                            val original = BugseeAndroidUtils.convertReportFieldsFromNative(report)
                            val fields = BugseeAndroidUtils.convertReportFieldsFromNative(report)
                            BugseeAndroidUtils.applyReportFieldsToNative(filter.invoke(fields), original, report)
                        }
                    } catch (e: Throwable) {
                        Logger.e(TAG, "exception during report fields post-filter execution", e)
                    }

                    try {
                        attachmentsProvider?.invoke(BugseeAndroidUtils.convertReport(report))?.let { attachments ->
                            BugseeAndroidUtils.addProviderAttachments(attachments, report)
                        }
                    } catch (e: Throwable) {
                        Logger.e(TAG, "exception during report attachments provider execution", e)
                    }
                } finally {
                    completionCallback.run()
                }
            }
        })
    }


    // View hierarchy management
    public actual fun captureViewHierarchy() {
        BugseeSDK.captureViewHierarchy()
    }
}

// The SDK's sanitizer is a stateless singleton that works in any SDK state; it is looked up on each
// call, as the SDK's own filter example does.
private object DefaultNetworkSanitizer : BugseeNetworkSanitizer {
    override fun sanitize(event: BugseeNetworkEvent) {
        BugseeSDK.getDefaultNetworkSanitizer().sanitize(event.underlyingEvent)
    }
}
