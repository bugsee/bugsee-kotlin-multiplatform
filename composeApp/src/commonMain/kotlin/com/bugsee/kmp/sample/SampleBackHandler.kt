package com.bugsee.kmp.sample

import androidx.compose.runtime.Composable

/** Handles the Android system Back while [enabled]. On iOS it does nothing: the screens have Back buttons. */
@Composable
expect fun SampleBackHandler(enabled: Boolean, onBack: () -> Unit)
