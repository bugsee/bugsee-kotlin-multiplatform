package com.bugsee.kmp.sample

import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeAttachment
import com.bugsee.kmp.BugseeLogLevel
import com.bugsee.kmp.sample.composeapp.generated.resources.Res

private const val TAG = "SampleHandlers"

private const val PRE_FILTER_PREFIX = "{ KMP :"
private const val POST_FILTER_SUFFIX = ": KMP }"
private const val PRE_FILTER_LABEL = "KMP:"

// Headers the network filter masks. A network filter replaces the SDK's built-in redaction, so a
// filter that keeps events has to redact secrets itself.
private val redactedHeaders = setOf("authorization", "cookie")

/**
 * The filters, listeners and providers of the Filters & Listeners and Report Filters screens.
 *
 * The ones that are on are kept across restarts and set again before Bugsee launches
 * ([launchSampleBugsee]): both SDKs build a crash report on the next launch, with the handlers set
 * at that moment.
 */
enum class SampleHandler {
    LogFilter,
    NetworkFilter,
    LifecycleListener,
    AttachmentsProvider,
    ReportPreFilter,
    ReportPostFilter;

    /** Sets or clears this handler in Bugsee and remembers the choice. */
    fun turn(on: Boolean) {
        SampleSettings.setHandlerOn(this, on)
        install(on)
    }

    internal fun install(on: Boolean) {
        when (this) {
            LogFilter -> Bugsee.setLogFilter(
                if (on) { event -> event?.apply { message = "[Filtered] $message" } } else null
            )

            NetworkFilter -> Bugsee.setNetworkEventFilter(
                if (on) { event ->
                    event?.apply {
                        headers = headers?.mapValues { (name, value) ->
                            if (name.lowercase() in redactedHeaders) "[redacted by the sample]" else value
                        }
                        AppLogger.d(TAG, "Network filter: $method $url")
                    }
                } else null
            )

            LifecycleListener -> Bugsee.setLifecycleEventsListener(
                if (on) { event ->
                    AppLogger.d(TAG, "Lifecycle: $event")
                    Bugsee.log("Lifecycle: $event", BugseeLogLevel.Info)
                } else null
            )

            AttachmentsProvider -> Bugsee.setReportAttachmentsProvider(
                if (on) { _ -> sampleAttachments() } else null
            )

            // The SDKs may run the report filters more than once for one report, so each change
            // is made only once.
            ReportPreFilter -> Bugsee.setReportFieldsPreFilter(
                if (on) { fields ->
                    if (!fields.summary.startsWith(PRE_FILTER_PREFIX)) fields.summary = PRE_FILTER_PREFIX + fields.summary
                    if (!fields.description.startsWith(PRE_FILTER_PREFIX)) fields.description = PRE_FILTER_PREFIX + fields.description
                    if (PRE_FILTER_LABEL !in fields.labels) fields.labels += PRE_FILTER_LABEL
                } else null
            )

            ReportPostFilter -> Bugsee.setReportFieldsPostFilter(
                if (on) { fields ->
                    if (!fields.summary.endsWith(POST_FILTER_SUFFIX)) fields.summary += POST_FILTER_SUFFIX
                    if (!fields.description.endsWith(POST_FILTER_SUFFIX)) fields.description += POST_FILTER_SUFFIX
                    fields
                } else null
            )
        }
    }
}

/** Sets the handlers that are on; called before Bugsee launches. */
fun installSavedHandlers() {
    SampleHandler.entries.filter { SampleSettings.isHandlerOn(it) }.forEach { it.install(true) }
}

private fun sampleAttachments(): List<BugseeAttachment> {
    // 1. Byte array attachment
    val fromBytes = BugseeAttachment.create("from_bytes", "Hello from Bugsee".encodeToByteArray())

    // 2. JSON file via URI
    val fromJsonFile = BugseeAttachment.create("from_json_file", Res.getUri("files/sample_data.json"))

    // 3. Runtime-generated file
    val generatedFilePath = getPlatform().tempDir + "bugsee_generated.txt"
    writeTextFile(generatedFilePath, "Generated at runtime by Bugsee KMP sample")
    val fromGeneratedFile = BugseeAttachment.create("from_generated_file", generatedFilePath)

    return listOf(fromBytes, fromJsonFile, fromGeneratedFile)
}
