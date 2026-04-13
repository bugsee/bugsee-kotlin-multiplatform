/// BugseeProtect.android.kt
///
/// Created by Denis Sheikherev on 29.08.2022.
///


package com.bugsee.kmp.protect

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.viewinterop.AndroidView
import com.bugsee.kmp.Bugsee

// Usage:
/*
setContent {
    BugseeProtect {
        Text(text = "Confidential.")
    }
}
*/

private enum class SlotsEnum { Main, Dependent }

@Composable
public actual fun BugseeProtect(
    modifier: Modifier,
    placeMainContent: Boolean,
    content: @Composable () -> Unit
) {
    SubcomposeLayout(
        modifier = modifier
    ) { constraints: Constraints ->

        val mainPlaceables: List<Placeable> = subcompose(SlotsEnum.Main, content)
            .map {
                it.measure(constraints.copy(minWidth = 0, minHeight = 0))
            }

        // Lay out children like a Box: stack at (0,0), overall size = largest child
        var maxWidth = 0
        var maxHeight = 0

        mainPlaceables.forEach { placeable: Placeable ->
            maxWidth = maxOf(maxWidth, placeable.width)
            maxHeight = maxOf(maxHeight, placeable.height)
        }

        val dependentPlaceables: List<Placeable> = subcompose(SlotsEnum.Dependent) {
            BugseeOverlay(Size(maxWidth.toFloat(), maxHeight.toFloat()))
        }
            .map { measurable: Measurable ->
                measurable.measure(Constraints.fixed(maxWidth, maxHeight))
            }

        layout(maxWidth, maxHeight) {

            if (placeMainContent) {
                mainPlaceables.forEach { placeable: Placeable ->
                    placeable.placeRelative(0, 0)
                }
            }

            dependentPlaceables.forEach { placeable: Placeable ->
                placeable.placeRelative(0, 0)
            }
        }
    }
}

@Composable
private fun BugseeOverlay(size: Size) {
    AndroidView(
        factory = { ctx ->
            View(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(size.width.toInt(), size.height.toInt())
                alpha = 0.0F
            }
        }, update = { view ->
            view.layoutParams = view.layoutParams?.apply {
                width = size.width.toInt()
                height = size.height.toInt()
            } ?: ViewGroup.LayoutParams(size.width.toInt(), size.height.toInt())
            Bugsee.addSecureView(view)
        }
    )
}
