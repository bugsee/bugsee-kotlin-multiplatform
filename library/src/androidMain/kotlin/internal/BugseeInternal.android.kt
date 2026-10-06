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
    public actual fun launch(apiKey: String, options: Map<String, Any>?) {
        val context = applicationContext ?: run {
            Logger.d(TAG, "Bugsee KMP: cannot launch() — applicationContext is not gathered")
            return
        }

        BugseeSDK.launch(context, apiKey, BugseeAndroidUtils.toSerializableMap(options, "launch"))
    }

    public actual fun launch(apiKey: String) {
        BugseeSDK.launch(apiKey)
    }

    public actual fun launch(apiKey: String, options: BugseeLaunchOptions?) {
        launch(apiKey, options?.toMap())
    }

    public actual fun stop() {
        BugseeSDK.stop()
    }

    public actual fun relaunch() {
        BugseeSDK.relaunch()
    }

    public actual fun relaunch(options: BugseeLaunchOptions?) {
        relaunch(options?.toMap())
    }

    public actual fun relaunch(options: Map<String, Any>?) {
        BugseeSDK.relaunch(BugseeAndroidUtils.toSerializableMap(options, "relaunch"))
    }


    // Feedback methods
    // In 7.x feedback lives in the separate bugsee-android-feedback module; these become
    // available again through the KMP feedback module.
    public actual fun showFeedback() {
        Logger.e(TAG, "showFeedback: feedback is not available in this build")
    }

    public actual fun setOnNewFeedbackListener(listener: BugseeFeedbackEventListener) {
        Logger.e(TAG, "setOnNewFeedbackListener: feedback is not available in this build")
    }

    public actual fun setDefaultFeedbackGreeting(greeting: String) {
        Logger.e(TAG, "setDefaultFeedbackGreeting: feedback is not available in this build")
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
        BugseeSDK.trace(traceName, value)
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

        BugseeSDK.event(eventName, HashMap(params))
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

    public actual fun logException(ex: Throwable, options: BugseeExceptionLoggingOptions?) {
        BugseeSDK.logException(ex, BugseeAndroidUtils.convertExceptionLoggingOptions(options))
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
    // callback.run(null) drops it. On a filter exception the original event is kept.
    public actual fun setNetworkEventFilter(filter: BugseeNetworkFilter?) {
        if (filter == null) {
            BugseeSDK.setNetworkEventFilter(null)
            return
        }

        BugseeSDK.setNetworkEventFilter(object : EventFilter<NetworkEvent> {
            override fun filter(event: NetworkEvent, callback: Callback1<NetworkEvent>) {
                // Timing updates have no 0.1.x stage; don't let a filter written for
                // BugseeNetworkEventStage.Before drop them.
                if (event.networkEventType == NetworkEvent.NetworkEventStage.RequestTimingsReceived) {
                    callback.run(event)
                    return
                }

                val result = try {
                    filter.invoke(BugseeNetworkEvent(event))?.underlyingEvent
                } catch (e: Exception) {
                    Logger.e(TAG, "exception during setNetworkEventFilter execution", e)
                    event
                }
                callback.run(result)
            }
        })
    }

    public actual fun setLogFilter(filter: BugseeLogFilter?) {
        if (filter == null) {
            BugseeSDK.setLogEventFilter(null)
            return
        }

        BugseeSDK.setLogEventFilter(object : EventFilter<LogEvent> {
            override fun filter(event: LogEvent, callback: Callback1<LogEvent>) {
                val result = try {
                    filter.invoke(BugseeLogEvent(event))?.underlyingEvent
                } catch (e: Exception) {
                    Logger.e(TAG, "exception during setLogFilter execution", e)
                    event
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

                try {
                    listener.invoke(converted)
                } catch (e: Exception) {
                    Logger.e(TAG, "exception during setLifecycleEventsListener execution", e)
                }
            }
        })
    }

    // User management methods
    // 7.x removed the global email; it is replaced by the user identifier, which the
    // user-identity API will expose.
    public actual fun setEmail(email: String) {
        Logger.e(TAG, "setEmail: not supported by Android SDK 7.x — ignoring")
    }

    public actual fun getEmail(): String? {
        Logger.e(TAG, "getEmail: not supported by Android SDK 7.x — returning null")
        return null
    }

    public actual fun clearEmail() {
        Logger.e(TAG, "clearEmail: not supported by Android SDK 7.x — ignoring")
    }


    // Attribute methods
    public actual fun setAttribute(name: String, value: Any) {
        if (value is Serializable) {
            BugseeSDK.setAttribute(name, value)
        } else {
            Logger.e(TAG, "setAttribute: value for '$name' is not Serializable (${value::class.simpleName}) — ignoring")
        }
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
    // 6.x deleted everything collected; includingIntermediate = true keeps that behavior.
    public actual fun deleteCollectedDataOnDevice(deletionEventListener: EventHandler<Boolean>?) {
        BugseeSDK.deleteCollectedDataOnDevice(
            true,
            deletionEventListener?.let { listener -> Callback1<Boolean> { listener.invoke(it == true) } }
        )
    }


    // Extended report methods
    public actual fun createReport(provider: BugseeExtendedReportProvider) {
        BugseeSDK.createReport(ReportCreationListener { report ->
            if (report == null) {
                Logger.d(TAG, "createReport: native SDK returned null, provider not invoked")
                return@ReportCreationListener
            }

            provider.invoke(BugseeExtendedReport(report))
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
                } catch (e: Exception) {
                    Logger.e(TAG, "exception during report fields pre-filter execution", e)
                } finally {
                    completionCallback.run()
                }
            }

            override fun onAfterReportCreated(report: Report, isTerminating: Boolean, completionCallback: Runnable) {
                // The post-filter and the attachments provider were independent hooks in 6.x:
                // a failure in one must not cancel the other.
                try {
                    reportFieldsFilter?.let { filter ->
                        val original = BugseeAndroidUtils.convertReportFieldsFromNative(report)
                        val fields = BugseeAndroidUtils.convertReportFieldsFromNative(report)
                        BugseeAndroidUtils.applyReportFieldsToNative(filter.invoke(fields), original, report)
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "exception during report fields post-filter execution", e)
                }

                try {
                    attachmentsProvider?.invoke(BugseeAndroidUtils.convertReport(report))?.let { attachments ->
                        BugseeAndroidUtils.addProviderAttachments(attachments, report)
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "exception during report attachments provider execution", e)
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
