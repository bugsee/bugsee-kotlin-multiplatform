package com.bugsee.kmp.internal

import android.app.Application
import android.view.View
import com.bugsee.kmp.*
import com.bugsee.library.attachment.CustomAttachment
import com.bugsee.library.attachment.ExtendedReport
import com.bugsee.library.attachment.Report
import com.bugsee.library.attachment.ReportAttachmentsProvider
import com.bugsee.library.data.IssueSeverity
import com.bugsee.library.lifecycle.LifecycleEventTypes
import com.bugsee.library.logs.BugseeLog
import com.bugsee.library.logs.LogListener
import com.bugsee.library.network.NetworkEventListener
import com.bugsee.library.network.data.BugseeNetworkEvent
import com.bugsee.library.send.OnChangeReportFieldsListener
import com.bugsee.library.send.ReportFields
import com.bugsee.library.send.ReportFieldsFilter

private typealias BugseeSDK = com.bugsee.library.Bugsee

public actual class BugseeInternal {

    private var reportFieldsFiller: BugseeReportFieldsFiller? = null
    private var reportFieldsFilter: BugseeReportFieldsFilter? = null

    public actual val appearance: BugseeAppearance
        get() = BugseeAppearance(BugseeSDK.getAppearance())

    // Launch methods - these are already working
    public actual fun launch(apiKey: String, options: Map<String, Any>?) {
        val context = applicationContext ?: run {
            Logger.d("BugseeInternal", "Bugsee KMP: cannot launch() — applicationContext is not gathered")
            return
        }

        val app: Application = context as Application
        BugseeSDK.launch(app, apiKey, options?.let { HashMap(it) } ?: HashMap())
    }

    public actual fun launch(apiKey: String) {
        val context = applicationContext ?: run {
            Logger.d("BugseeInternal", "Bugsee KMP: cannot launch() — applicationContext is not gathered")
            return
        }

        val app: Application = context as Application
        BugseeSDK.launch(app, apiKey)
    }

    public actual fun launch(apiKey: String, options: BugseeLaunchOptions?) {
        val context = applicationContext ?: run {
            Logger.d("BugseeInternal", "Bugsee KMP: cannot launch() — applicationContext is not gathered")
            return
        }

        val app: Application = context as Application
        BugseeSDK.launch(app, apiKey, if (options != null) HashMap(options.toMap()) else HashMap())
    }

    public actual fun stop() {
        BugseeSDK.stop()
    }

    public actual fun relaunch() {
        BugseeSDK.relaunch()
    }

    public actual fun relaunch(options: BugseeLaunchOptions?) {
        BugseeSDK.relaunch(if (options != null) HashMap(options.toMap()) else HashMap())
    }

    public actual fun relaunch(options: Map<String, Any>?) {
        BugseeSDK.relaunch(options?.let { HashMap(it) } ?: HashMap())
    }


    // Feedback methods
    public actual fun showFeedback() {
        val context = applicationContext ?: run {
            Logger.d("BugseeInternal", "Bugsee KMP: cannot launch() — applicationContext is not gathered")
            return
        }

        val app: Application = context as Application
        BugseeSDK.showFeedbackActivity(app)
    }

    public actual fun setOnNewFeedbackListener(listener: BugseeFeedbackEventListener) {
        BugseeSDK.setOnNewFeedbackListener(listener)
    }

    public actual fun setDefaultFeedbackGreeting(greeting: String) {
        BugseeSDK.setDefaultFeedbackGreeting(greeting)
    }

    // Network logging methods
//    public actual fun addNetworkLoggingToOkHttpBuilder(clientBuilder: OkHttpClient.Builder): OkHttpClient.Builder {
//        return BugseeSDK.addNetworkLoggingToOkHttpBuilder(clientBuilder)
//    }
//
//    public actual fun addNetworkLoggingToOkHttpClient(client: com.squareup.okhttp.OkHttpClient) {
//        BugseeSDK.addNetworkLoggingToOkHttpClient(client)
//    }
//
//    public actual fun addNetworkLoggingToKtorHttpClient(client: HttpClient) {
//        BugseeSDK.addNetworkLoggingToKtorHttpClient(client)
//    }
//
//    public actual fun addNetworkLoggingToPicassoDownloader(downloader: OkHttp3Downloader): Boolean {
//        return BugseeSDK.addNetworkLoggingToPicassoDownloader(downloader)
//    }
//
//    public actual fun newOkHttpWrappedWebSocket(
//        okHttpClient: OkHttpClient,
//        request: Request,
//        listener: WebSocketListener
//    ): WebSocket {
//        return BugseeSDK.newOkHttpWrappedWebSocket(okHttpClient, request, listener)
//    }


    // Logging methods
    public actual fun log(message: String) {
        BugseeSDK.log(message)
    }

    public actual fun log(message: String, level: BugseeLogLevel) {
        BugseeSDK.log(
            message,
            BugseeAndroidUtils.Companion.convertLogLevel(level)
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
            BugseeAndroidUtils.Companion.convertSeverity(severity)
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
            IssueSeverity.fromIntValue(severity.getLevel()),
            ArrayList(labels)
        )
    }


    // Bug report upload methods
    public actual fun upload(summary: String, description: String, severity: BugseeSeverity) {
        BugseeSDK.upload(summary, description, BugseeAndroidUtils.Companion.convertSeverity(severity))
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
            BugseeAndroidUtils.Companion.convertSeverity(severity),
            ArrayList(labels)
        )
    }

    public actual fun upload(
        summary: String,
        description: String,
        severity: BugseeSeverity,
        labels: List<String>?,
        includeVideo: Boolean
    ) {
        BugseeSDK.upload(
            summary,
            description,
            BugseeAndroidUtils.Companion.convertSeverity(severity),
            if (labels == null) ArrayList() else ArrayList(labels),
            includeVideo
        )
    }


    // Exception logging methods
    public actual fun logException(ex: Throwable) {
        BugseeSDK.logException(ex)
    }

    public actual fun logException(ex: Throwable, options: BugseeExceptionLoggingOptions?) {
        BugseeSDK.logException(ex, BugseeAndroidUtils.Companion.convertExceptionLoggingOptions(options))
    }

    // Privacy control methods
    public actual fun pause() {
        BugseeSDK.pause()
    }

    public actual fun resume() {
        BugseeSDK.resume()
    }


    // Status checks
    public actual fun isLaunched(): Boolean {
        return BugseeSDK.getLaunched()
    }

    // Secure view methods
    public actual fun addSecureView(view: Any?) {
        if (view is View) {
            BugseeSDK.addSecureView(view)
        }
    }

    public actual fun removeSecureView(view: Any?) {
        if (view is View) {
            BugseeSDK.removeSecureView(view)
        }
    }


    // Filter and listener methods
    public actual fun setNetworkEventFilter(filter: BugseeNetworkFilter?) {
        if (filter == null) {
            BugseeSDK.setNetworkEventFilter(null)
            return
        }

        BugseeSDK.setNetworkEventFilter(
            object : com.bugsee.library.network.NetworkEventFilter {
                override fun filter(
                    p0: BugseeNetworkEvent?,
                    p1: NetworkEventListener?
                ) {
                    try {
                        if (p0 == null || p1 == null) {
                            p1?.onEvent(p0);
                            return
                        }

                        val filteredEvent = filter.invoke(
                            BugseeNetworkEvent(
                                p0
                            )
                        )
                        if (filteredEvent == null) {
                            return
                        }

                        p1?.onEvent(filteredEvent.underlyingEvent)
                    } catch (e: Exception) {
                        Logger.e("BugseeInternal", "exception during setNetworkEventFilter execution: ${e.message}")
                        Bugsee.log("BugseeInternal.android: exception during setNetworkEventFilter execution: ${e.message}", BugseeLogLevel.Warning)
                        p1?.onEvent(p0)
                    }
                }
            }
        )
    }

    public actual fun setLogFilter(filter: BugseeLogFilter?) {
        if (filter == null) {
            BugseeSDK.setLogFilter(null)
            return
        }

        BugseeSDK.setLogFilter(
            object : com.bugsee.library.logs.LogFilter {
                override fun filter(
                    p0: BugseeLog?,
                    p1: LogListener?
                ) {
                    try {
                        if (p0 == null || p1 == null) {
                            p1?.onLog(p0);
                            return
                        }

                        val filteredEvent = filter.invoke(
                            BugseeLogEvent(
                                p0
                            )
                        )
                        if (filteredEvent == null) {
                            return
                        }

                        p1.onLog(filteredEvent.underlyingEvent)
                    } catch (e: Exception) {
                        Logger.e("BugseeInternal", "exception during setLogFilter execution: ${e.message}")
                        Bugsee.log("BugseeInternal: exception during setLogFilter execution: ${e.message}", BugseeLogLevel.Warning)
                        p1?.onLog(p0)
                    }
                }
            }
        )
    }

    public actual fun setLifecycleEventsListener(listener: BugseeLifecycleEventListener?) {
        if (listener == null) {
            BugseeSDK.setLifecycleEventsListener(null)
            return
        }

        BugseeSDK.setLifecycleEventsListener(
            object : com.bugsee.library.lifecycle.LifecycleEventListener {
                override fun onEvent(p0: LifecycleEventTypes?) {
                    if (p0 == null) {
                        return
                    }

                    try {
                        listener.invoke(BugseeAndroidUtils.Companion.convertLifecycleEvent(p0))
                    } catch (e: Exception) {
                        Logger.e("BugseeInternal", "exception during setLifecycleEventsListener execution: ${e.message}")
                        Bugsee.log("BugseeInternal.android: exception during setLifecycleEventsListener execution: ${e.message}", BugseeLogLevel.Warning)
                    }
                }
            }
        )
    }

    // User management methods
    public actual fun setEmail(email: String) {
        BugseeSDK.setEmail(email)
    }

    public actual fun getEmail(): String? {
        return BugseeSDK.getEmail()
    }

    public actual fun clearEmail() {
        BugseeSDK.setEmail(null)
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
        if (provider == null) {
            BugseeSDK.setReportAttachmentsProvider(null)
            return
        }

        BugseeSDK.setReportAttachmentsProvider(
            object : ReportAttachmentsProvider {
                override fun getAttachments(p0: Report?): ArrayList<CustomAttachment?>? {
                    if (p0 == null) {
                        return null
                    }

                    val gatheredAttachments = provider.invoke(BugseeAndroidUtils.convertReport(p0))
                    if (gatheredAttachments == null) {
                        return null
                    }

                    return ArrayList(gatheredAttachments.map(BugseeAndroidUtils.Companion::convertAttachment))
                }
            }
        )
    }


    // Data management
    public actual fun deleteCollectedDataOnDevice(deletionEventListener: EventHandler<Boolean>?) {
        BugseeSDK.deleteCollectedDataOnDevice(deletionEventListener)
    }


    // Extended report methods
    public actual fun createReport(provider: BugseeExtendedReportProvider) {
        BugseeSDK.createReport(
            object : com.bugsee.library.Bugsee.ExtendedReportCreatedListener {
                override fun onCreated(p0: ExtendedReport?) {
                    if (p0 == null) {
                        Logger.d("BugseeInternal", "createReport: native SDK returned null, provider not invoked")
                        return
                    }

                    provider.invoke(BugseeAndroidUtils.Companion.convertExtendedReport(p0))
                }
            }
        )
    }

    public actual fun upload(report: BugseeExtendedReport) {
        BugseeSDK.upload(BugseeAndroidUtils.Companion.convertExtendedReport(report))
    }


    // Report fields filter
    public actual fun setReportFieldsPreFilter(filler: BugseeReportFieldsFiller?) {
        reportFieldsFiller = filler
        synchronizeReportFilter()
    }

    public actual fun setReportFieldsPostFilter(filter: BugseeReportFieldsFilter?) {
        reportFieldsFilter = filter
        synchronizeReportFilter()
    }

    private fun synchronizeReportFilter() {
        if (reportFieldsFiller == null && reportFieldsFilter == null) {
            BugseeSDK.setReportFieldsFilter(null)
            return
        }

        BugseeSDK.setReportFieldsFilter(
            object : ReportFieldsFilter {
                override fun addFieldsBeforeReportCreated(
                    p0: ReportFields?,
                    p1: OnChangeReportFieldsListener?
                ) {
                    // Use copy here to avoid race conditions
                    // in the logic below
                    val filler = reportFieldsFiller

                    if (filler == null || p0 == null) {
                        p1?.onChanged(p0)
                        return
                    }

                    val convertedFields = BugseeAndroidUtils.convertReportFieldsFromNative(p0)
                    filler.invoke(convertedFields)

                    p1?.onChanged(BugseeAndroidUtils.convertReportFieldsToNative(convertedFields))
                }

                override fun changeFieldsAfterReportCreated(
                    p0: ReportFields?,
                    p1: OnChangeReportFieldsListener?
                ) {
                    val filter = reportFieldsFilter

                    if (filter == null || p0 == null) {
                        p1?.onChanged(p0);
                        return
                    }

                    val convertedFields = BugseeAndroidUtils.convertReportFieldsFromNative(p0)
                    filter.invoke(convertedFields)

                    p1?.onChanged(BugseeAndroidUtils.convertReportFieldsToNative(convertedFields))
                }
            }
        )
    }


    // View hierarchy management
    public actual fun captureViewHierarchy() {
        BugseeSDK.captureViewHierarchy()
    }
}