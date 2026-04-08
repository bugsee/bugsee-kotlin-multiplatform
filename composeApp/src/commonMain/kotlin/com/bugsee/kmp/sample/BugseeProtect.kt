package com.bugsee.kmp.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Wraps Compose content with a Bugsee secure overlay, hiding the protected area
 * from Bugsee video recordings and screenshots.
 *
 * On Android, an invisible [android.view.View] overlay is placed on top of the content
 * and registered via [com.bugsee.kmp.Bugsee.addSecureView].
 * On iOS, an invisible [platform.UIKit.UIView] overlay is placed on top of the content
 * and registered via [com.bugsee.kmp.Bugsee.addSecureView].
 *
 * **Important:** [com.bugsee.kmp.Bugsee.launch] must be called before this composable
 * enters composition. Calling it from a platform entry point (e.g. `Activity.onCreate`,
 * iOS `AppDelegate`) is recommended.
 *
 * @param modifier Modifier applied to the outer layout.
 * @param placeMainContent If false, the content is measured but not placed (only the
 *   secure overlay is drawn). Defaults to true.
 * @param content The composable content to protect.
 */
@Composable
expect fun BugseeProtect(
    modifier: Modifier = Modifier,
    placeMainContent: Boolean = true,
    content: @Composable () -> Unit
)
