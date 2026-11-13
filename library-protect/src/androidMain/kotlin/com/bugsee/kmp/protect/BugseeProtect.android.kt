/// BugseeProtect.android.kt
///
/// Created by Denis Sheikherev on 29.08.2022.
///


package com.bugsee.kmp.protect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import com.bugsee.library.compose.bugseeSecure

// Usage:
/*
setContent {
    BugseeProtect {
        Text(text = "Confidential.")
    }
}
*/

// The node is marked with the Android SDK's Compose semantics key (bugsee-android-compose), which
// the SDK's Compose scanner finds on every captured frame. Unlike a view registered with
// Bugsee.addSecureView, the mark is not erased by Bugsee.stop(): the scanner is registered again
// when the SDK launches, so the content stays hidden after a relaunch or Stop and Launch.
@Composable
public actual fun BugseeProtect(
    modifier: Modifier,
    placeMainContent: Boolean,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier.bugseeSecure()) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        // Lay out children like a Box: stack at (0,0), overall size = largest child
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.maxOfOrNull { it.height } ?: 0

        layout(width, height) {
            if (placeMainContent) {
                placeables.forEach { it.placeRelative(0, 0) }
            }
        }
    }
}
