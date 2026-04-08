package com.bugsee.kmp.sample.screens

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeAttachment
import com.bugsee.kmp.BugseeLogLevel
import com.bugsee.kmp.sample.composeapp.generated.resources.Res
import com.bugsee.kmp.sample.getPlatform
import com.bugsee.kmp.sample.writeTextFile
import io.ktor.utils.io.core.toByteArray

@Composable
fun FiltersListenersScreen(
    logFilterActive: Boolean,
    onLogFilterChange: (Boolean) -> Unit,
    networkFilterActive: Boolean,
    onNetworkFilterChange: (Boolean) -> Unit,
    lifecycleListenerActive: Boolean,
    onLifecycleListenerChange: (Boolean) -> Unit,
    attachmentsProviderActive: Boolean,
    onAttachmentsProviderChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    TestScreenScaffold(title = "Filters & Listeners", onBackClick = onBackClick) {
        TestSectionLabel("Log Filter")
        if (logFilterActive) Text("Log filter active")
        TestButton(if (logFilterActive) "Clear Log Filter" else "Set Log Filter") {
            if (logFilterActive) {
                Bugsee.setLogFilter(null)
                onLogFilterChange(false)
            } else {
                Bugsee.setLogFilter { event ->
                    event?.message = "[Filtered] ${event?.message}"
                    event
                }
                onLogFilterChange(true)
            }
        }

        HorizontalDivider()
        TestSectionLabel("Network Filter")
        if (networkFilterActive) Text("Network filter active")
        TestButton(if (networkFilterActive) "Clear Network Filter" else "Set Network Filter") {
            if (networkFilterActive) {
                Bugsee.setNetworkEventFilter(null)
                onNetworkFilterChange(false)
            } else {
                Bugsee.setNetworkEventFilter { event ->
                    Bugsee.log("[Filtered] ${event?.method} ${event?.url}", BugseeLogLevel.Info)
                    event
                }
                onNetworkFilterChange(true)
            }
        }

        HorizontalDivider()
        TestSectionLabel("Lifecycle Listener")
        if (lifecycleListenerActive) Text("Lifecycle listener active")
        TestButton(if (lifecycleListenerActive) "Clear Lifecycle Listener" else "Set Lifecycle Listener") {
            if (lifecycleListenerActive) {
                Bugsee.setLifecycleEventsListener(null)
                onLifecycleListenerChange(false)
            } else {
                Bugsee.setLifecycleEventsListener { event ->
                    Bugsee.log("Lifecycle: $event", BugseeLogLevel.Info)
                }
                onLifecycleListenerChange(true)
            }
        }

        HorizontalDivider()
        TestSectionLabel("Attachments Provider")
        if (attachmentsProviderActive) Text("Attachments provider active")
        TestButton(if (attachmentsProviderActive) "Clear Attachments Provider" else "Set Attachments Provider") {
            if (attachmentsProviderActive) {
                Bugsee.setReportAttachmentsProvider(null)
                onAttachmentsProviderChange(false)
            } else {
                Bugsee.setReportAttachmentsProvider { bugseeReport ->
                    // 1. Byte array attachment
                    val str = "Hello from Bugsee"
                    val attachment1 = BugseeAttachment.create("from_bytes", str.toByteArray())

                    // 2. JSON file via URI
                    val jsonUri = Res.getUri("files/sample_data.json")
                    val attachment2 = BugseeAttachment.create("from_json_file", jsonUri)

                    // 3. Runtime-generated file
                    val generatedFilePath = getPlatform().tempDir + "bugsee_generated.txt"
                    writeTextFile(generatedFilePath, "Generated at runtime by Bugsee KMP sample")
                    val attachment3 = BugseeAttachment.create("from_generated_file", generatedFilePath)

                    listOf(attachment1, attachment2, attachment3)
                }

                onAttachmentsProviderChange(true)
            }
        }
    }
}
