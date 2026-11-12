package com.bugsee.kmp.sample

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun SampleBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
