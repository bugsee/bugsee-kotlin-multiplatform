package com.bugsee.kmp.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.feedback.BugseeFeedback
import kotlin.reflect.KMutableProperty0

// Fixed colors, so it is visible which screens apply them and whether they stay the same in
// both system themes.
private val testColors = mapOf(
    "reportBackgroundColor" to 0xFFFFF3E0.toInt(),
    "reportTextColor" to 0xFF3E2723.toInt(),
    "reportActionBarColor" to 0xFFE65100.toInt(), // Android only
    "feedbackBackgroundColor" to 0xFFE8F5E9.toInt(),
    "feedbackIncomingBubbleColor" to 0xFFC8E6C9.toInt(),
    "feedbackOutgoingBubbleColor" to 0xFF2E7D32.toInt(),
    "feedbackTitleTextColor" to 0xFF1B5E20.toInt(),
)

// Colors that iOS does not have: on iOS they read "not set" before and after Apply test colors.
private val androidOnlyColors = setOf("reportActionBarColor")

private fun colorProperties(): List<Pair<String, KMutableProperty0<Int?>>> {
    val appearance = Bugsee.appearance
    return listOf(
        "reportBackgroundColor" to appearance::reportBackgroundColor,
        "reportTextColor" to appearance::reportTextColor,
        "reportActionBarColor" to appearance::reportActionBarColor,
        "reportVersionColor" to appearance::reportVersionColor,
        "feedbackBackgroundColor" to appearance::feedbackBackgroundColor,
        "feedbackIncomingBubbleColor" to appearance::feedbackIncomingBubbleColor,
        "feedbackOutgoingBubbleColor" to appearance::feedbackOutgoingBubbleColor,
        "feedbackTitleTextColor" to appearance::feedbackTitleTextColor,
        "feedbackEmailBackgroundColor" to appearance::feedbackEmailBackgroundColor,
    )
}

private fun hex(value: Int?): String =
    value?.let { "#" + it.toUInt().toString(16).uppercase().padStart(8, '0') } ?: "not set"

/**
 * Device check for appearance colors in the light and the dark system theme: what the getters
 * return for the SDK's own colors, and how the report and feedback screens look with colors set
 * from Kotlin (an ARGB Int is one color for both themes).
 */
@Composable
fun AppearanceScreen(onBackClick: () -> Unit) {
    val dark = isSystemInDarkTheme()
    var generation by remember { mutableStateOf(0) }

    TestScreenScaffold(title = "Appearance", onBackClick = onBackClick) {
        Text("System theme: ${if (dark) "dark" else "light"}")
        Text(
            "Switch the system theme, come back and tap Refresh: a color the SDK keeps for both " +
                "themes reads differently in each. Open the report and feedback screens in both themes, " +
                "before and after Apply test colors.",
            style = MaterialTheme.typography.bodySmall,
        )

        key(generation, dark) {
            colorProperties().forEach { (name, property) ->
                val value = property.get()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.size(20.dp).background(value?.let { Color(it) } ?: Color.Transparent))
                    val androidOnly = if (name in androidOnlyColors) " (Android only)" else ""
                    Text("$name$androidOnly: ${hex(value)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        TestButton("Refresh") { generation++ }
        TestButton("Apply test colors") {
            colorProperties().forEach { (name, property) -> testColors[name]?.let { property.set(it) } }
            generation++
        }
        TestButton("Reset test colors (set to null)") {
            colorProperties().forEach { (name, property) -> if (name in testColors) property.set(null) }
            generation++
        }
        TestButton("Show Report Dialog") { Bugsee.showReportDialog() }
        TestButton("Show Feedback") { BugseeFeedback.show() }
    }
}
