/// BugseeProtect.ios.kt
///
/// Created by Denis Sheikherev on 08.04.2026.
///
/// The MIT License (MIT)
///
/// Copyright (c) 2026 Bugsee
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in
/// all copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
/// THE SOFTWARE.
///

package com.bugsee.kmp.sample

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
actual fun BugseeProtect(
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
