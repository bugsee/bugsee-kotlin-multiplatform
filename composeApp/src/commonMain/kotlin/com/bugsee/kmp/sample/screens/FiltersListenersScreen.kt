package com.bugsee.kmp.sample.screens

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bugsee.kmp.sample.AppLogger
import com.bugsee.kmp.sample.SampleHandler

private const val TAG = "FiltersListeners"

/** Shown on the screens that set handlers: why they survive a restart. */
internal const val HANDLERS_KEPT_NOTE =
    "What you set here is kept across restarts and set again before Bugsee launches, so the report " +
        "of a crash, which is built on the next launch, gets it too."

@Composable
fun FiltersListenersScreen(
    handlersOn: Map<SampleHandler, Boolean>,
    onTurn: (SampleHandler, Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    TestScreenScaffold(title = "Filters & Listeners", onBackClick = onBackClick) {
        Note(HANDLERS_KEPT_NOTE)

        HandlerToggle("Log Filter", SampleHandler.LogFilter, handlersOn, onTurn)
        Note(
            "Adds \"[Filtered]\" to captured console logs (Logcat, NSLog). Messages sent with " +
                "Bugsee.log() are not filtered."
        )
        TestButton("Write a Console Log Line") {
            AppLogger.i(TAG, "A console line for the log filter")
        }

        HorizontalDivider()
        HandlerToggle("Network Filter", SampleHandler.NetworkFilter, handlersOn, onTurn)
        Note(
            "A network filter replaces the SDK's built-in redaction. This one masks the Authorization " +
                "and Cookie headers (Advanced Network Tests sends an Authorization header) and logs " +
                "each event to the console."
        )

        HorizontalDivider()
        HandlerToggle("Lifecycle Listener", SampleHandler.LifecycleListener, handlersOn, onTurn)
        Note("Logs each lifecycle event to the console and with Bugsee.log().")

        HorizontalDivider()
        HandlerToggle("Attachments Provider", SampleHandler.AttachmentsProvider, handlersOn, onTurn)
        Note("Adds three attachments to every report: bytes, a bundled JSON file and a generated file.")
    }
}

/** A section with a button that sets or clears [handler]. */
@Composable
internal fun HandlerToggle(
    title: String,
    handler: SampleHandler,
    handlersOn: Map<SampleHandler, Boolean>,
    onTurn: (SampleHandler, Boolean) -> Unit
) {
    val on = handlersOn[handler] == true
    TestSectionLabel(title)
    if (on) Text("$title active")
    TestButton(if (on) "Clear $title" else "Set $title") { onTurn(handler, !on) }
}

@Composable
internal fun Note(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall)
}
