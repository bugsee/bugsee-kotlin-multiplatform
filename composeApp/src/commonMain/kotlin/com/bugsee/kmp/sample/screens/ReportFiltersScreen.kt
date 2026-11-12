package com.bugsee.kmp.sample.screens

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import com.bugsee.kmp.sample.SampleHandler

@Composable
fun ReportFiltersScreen(
    handlersOn: Map<SampleHandler, Boolean>,
    onTurn: (SampleHandler, Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    TestScreenScaffold(title = "Report Filters", onBackClick = onBackClick) {
        Note(HANDLERS_KEPT_NOTE)

        HandlerToggle("Pre-Filter", SampleHandler.ReportPreFilter, handlersOn, onTurn)
        Note("Before the report is created: adds \"{ KMP :\" to the summary and the description, and the label \"KMP:\".")

        HorizontalDivider()
        HandlerToggle("Post-Filter", SampleHandler.ReportPostFilter, handlersOn, onTurn)
        Note("After the report is created: adds \": KMP }\" to the summary and the description.")

        HorizontalDivider()
        TestButton("Clear All Report Filters") {
            onTurn(SampleHandler.ReportPreFilter, false)
            onTurn(SampleHandler.ReportPostFilter, false)
        }
    }
}
