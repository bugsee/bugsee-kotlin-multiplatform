package com.bugsee.kmp.sample

import androidx.compose.runtime.Composable

/** Handles the system Back while [enabled]: on Android only, iOS has no system Back. */
@Composable
expect fun SampleBackHandler(enabled: Boolean, onBack: () -> Unit)
