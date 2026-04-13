/// BugseeProtect.ios.kt
///
/// Created by Denis Sheikherev on 08.04.2026.
///

package com.bugsee.kmp.protect

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import com.bugsee.kmp.Bugsee
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIScreen
import platform.UIKit.UIView

// Usage:
/*
setContent {
    BugseeProtect {
        Text(text = "Confidential.")
    }
}
*/

@OptIn(ExperimentalForeignApi::class)
@Composable
public actual fun BugseeProtect(
    modifier: Modifier,
    placeMainContent: Boolean,
    content: @Composable () -> Unit
) {
    val overlayView = remember {
        UIView().apply {
            opaque = false
            backgroundColor = UIColor.clearColor
            alpha = 0.0
            userInteractionEnabled = false
        }
    }

    DisposableEffect(Unit) {
        UIApplication.sharedApplication.keyWindow?.addSubview(overlayView)
        Bugsee.addSecureView(overlayView)

        onDispose {
            Bugsee.removeSecureView(overlayView)
            overlayView.removeFromSuperview()
        }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val scale = UIScreen.mainScreen.scale
            val position = coordinates.positionInWindow()
            val size = coordinates.size
            overlayView.setFrame(
                CGRectMake(
                    position.x.toDouble() / scale,
                    position.y.toDouble() / scale,
                    size.width.toDouble() / scale,
                    size.height.toDouble() / scale
                )
            )
            Bugsee.addSecureView(overlayView)
        }
    ) {
        if (placeMainContent) {
            content()
        }
    }
}
