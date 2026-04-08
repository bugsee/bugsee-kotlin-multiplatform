package com.bugsee.kmp.sample.screens

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bugsee.kmp.Bugsee

@Composable
fun ReportFiltersScreen(
    preFilterActive: Boolean,
    onPreFilterChange: (Boolean) -> Unit,
    postFilterActive: Boolean,
    onPostFilterChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    TestScreenScaffold(title = "Report Filters", onBackClick = onBackClick) {
        TestSectionLabel("Pre-Filter")
        if (preFilterActive) Text("Pre-filter active")
        TestButton(if (preFilterActive) "Clear Pre-Filter" else "Set Pre-Filter") {
            if (preFilterActive) {
                Bugsee.setReportFieldsPreFilter(null)
                onPreFilterChange(false)
            } else {
                Bugsee.setReportFieldsPreFilter {
                    it.summary = "{ KMP :" + it.summary
                    it.description = "{ KMP :" + it.description
                    it.labels += listOf("KMP:")
                }
                onPreFilterChange(true)
            }
        }

        HorizontalDivider()
        TestSectionLabel("Post-Filter")
        if (postFilterActive) Text("Post-filter active")
        TestButton(if (postFilterActive) "Clear Post-Filter" else "Set Post-Filter") {
            if (postFilterActive) {
                Bugsee.setReportFieldsPostFilter(null)
                onPostFilterChange(false)
            } else {
                Bugsee.setReportFieldsPostFilter {
                    it.summary += ": KMP }"
                    it.description += ": KMP }"
                    return@setReportFieldsPostFilter it
                }
                onPostFilterChange(true)
            }
        }

        HorizontalDivider()
        TestButton("Clear All Report Filters") {
            Bugsee.setReportFieldsPreFilter(null)
            Bugsee.setReportFieldsPostFilter(null)
            onPreFilterChange(false)
            onPostFilterChange(false)
        }
    }
}
