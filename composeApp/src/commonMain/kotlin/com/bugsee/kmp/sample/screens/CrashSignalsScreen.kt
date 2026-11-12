package com.bugsee.kmp.sample.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bugsee.kmp.BugseeCrashSignals
import com.bugsee.kmp.sample.NativeSignal
import com.bugsee.kmp.sample.SampleSettings
import com.bugsee.kmp.sample.badMemoryAccessAvailable
import com.bugsee.kmp.sample.causeBadMemoryAccess
import com.bugsee.kmp.sample.describeCrashSignals
import com.bugsee.kmp.sample.launchedCrashSignals
import com.bugsee.kmp.sample.nativeCrashHandlerInfo
import com.bugsee.kmp.sample.raiseNativeSignal

private val presets = listOf(
    "Default" to BugseeCrashSignals.Default,
    "All" to BugseeCrashSignals.All,
    "None" to BugseeCrashSignals.None,
    "All except SEGV" to (BugseeCrashSignals.All and BugseeCrashSignals.SEGV.inv()),
    "All except SEGV and BUS" to (BugseeCrashSignals.All and (BugseeCrashSignals.SEGV or BugseeCrashSignals.BUS).inv()),
    "ABRT only" to BugseeCrashSignals.ABRT,
)

/**
 * Device check for `captureCrashSignals`: choose the signal set the next launch uses, restart the
 * app, then send a signal to the app's own process and check whether Bugsee reports the crash on the
 * following launch.
 */
@Composable
fun CrashSignalsScreen(onBackClick: () -> Unit) {
    var saved by remember { mutableStateOf(SampleSettings.crashSignals) }

    TestScreenScaffold(title = "Crash Signals", onBackClick = onBackClick) {
        Text(nativeCrashHandlerInfo(), style = MaterialTheme.typography.bodySmall)
        Text("This launch: ${describeCrashSignals(launchedCrashSignals)}")
        Text("Next launch: ${describeCrashSignals(saved)}")
        if (saved != launchedCrashSignals) {
            Text(
                "Restart required: until the app is closed completely and opened again, crashes are " +
                    "handled with this launch's set.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Text(
            "captureCrashSignals is read once per process, when Bugsee first launches. After choosing a set, " +
                "close the app completely (swipe it away) and open it again from the home screen: with a " +
                "debugger attached the signal stops in the debugger, and the iOS SDK then installs no crash handler.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        TestSectionLabel("Signals to intercept from the next launch")
        presets.forEach { (label, value) ->
            TestButton(if (value == saved) "✓ $label" else label) {
                SampleSettings.crashSignals = value
                saved = value
            }
        }

        TestSectionLabel("Send a software signal to the app (it crashes)")
        Text(
            "A signal in this launch's set is reported as a crash on the next launch; one left out ends " +
                "the app without a report. On Android SIGQUIT and SIGSYS are always intercepted, and the NDK " +
                "handler treats a sent signal like a fault. On iOS a sent signal reaches only the BSD handler; " +
                "a real bad memory access goes through Mach exceptions and is reported while SEGV or BUS is " +
                "selected (check it with the button below and \"All except SEGV and BUS\").",
            style = MaterialTheme.typography.bodySmall,
        )
        NativeSignal.entries.forEach { signal ->
            TestButton("SIG${signal.name}") { raiseNativeSignal(signal) }
        }
        if (badMemoryAccessAvailable) {
            TestButton("Bad memory access (real fault)") { causeBadMemoryAccess() }
        }
    }
}
