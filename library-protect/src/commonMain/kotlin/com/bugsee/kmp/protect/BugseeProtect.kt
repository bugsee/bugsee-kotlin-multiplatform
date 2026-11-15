package com.bugsee.kmp.protect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Hides Compose content from Bugsee video recordings and screenshots.
 *
 * On Android, the content's node is marked with the Android SDK's `Modifier.bugseeSecure()`
 * (from `com.bugsee:bugsee-android-compose`, a dependency of this artifact), which the SDK finds
 * in the Compose tree on every frame. The protection survives [com.bugsee.kmp.Bugsee.stop] and
 * relaunches.
 * On iOS, an invisible [platform.UIKit.UIView] overlay is placed on top of the content
 * and registered via [com.bugsee.kmp.Bugsee.addSecureView].
 *
 * **Important (iOS):** [com.bugsee.kmp.Bugsee.launch] must be called before this composable
 * enters composition. Calling it from a platform entry point (e.g. iOS `AppDelegate`) is
 * recommended.
 *
 * @param modifier Modifier applied to the outer layout.
 * @param placeMainContent If false, the content is not shown, and only its area is hidden
 *   (on Android it is measured but not placed; on iOS it is not composed). Defaults to true.
 * @param content The composable content to protect.
 */
@Composable
public expect fun BugseeProtect(
    modifier: Modifier = Modifier,
    placeMainContent: Boolean = true,
    content: @Composable () -> Unit
)
