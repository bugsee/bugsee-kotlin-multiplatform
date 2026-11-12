package com.bugsee.kmp.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeSeverity
import com.bugsee.kmp.feedback.BugseeFeedback
import com.bugsee.kmp.protect.BugseeProtect
import com.bugsee.kmp.sample.screens.*
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

sealed class Screen {
    data object Main : Screen()
    data object NetworkTests : Screen()
    data object Exceptions : Screen()
    data object FiltersListeners : Screen()
    data object LoggingTests : Screen()
    data object CrashProbe : Screen()
    data object AdvancedNetwork : Screen()
    data object UserIdentifierTests : Screen()
    data object WebSocket : Screen()
    data object Attributes : Screen()
    data object ReportFilters : Screen()
    data object ExtendedReports : Screen()
    data object CrashSignals : Screen()
    data object Appearance : Screen()
}

private const val TAG = "App.kt"

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        // Hoisted state for filters & listeners (survives navigation)
        var preFilterActive by remember { mutableStateOf(false) }
        var postFilterActive by remember { mutableStateOf(false) }
        var logFilterActive by remember { mutableStateOf(false) }
        var networkFilterActive by remember { mutableStateOf(false) }
        var lifecycleListenerActive by remember { mutableStateOf(false) }
        var attachmentsProviderActive by remember { mutableStateOf(false) }

        // The system Back (Android) closes the menu, then returns to the main screen.
        SampleBackHandler(enabled = drawerState.isOpen || currentScreen != Screen.Main) {
            if (drawerState.isOpen) {
                scope.launch { drawerState.close() }
            } else {
                currentScreen = Screen.Main
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    onAction = { scope.launch { drawerState.close() } }
                )
            }
        ) {
            when (currentScreen) {
                Screen.Main -> MainScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigate = { currentScreen = it }
                )
                Screen.NetworkTests -> NetworkTestsScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.Exceptions -> ExceptionsScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.FiltersListeners -> FiltersListenersScreen(
                    logFilterActive = logFilterActive,
                    onLogFilterChange = { logFilterActive = it },
                    networkFilterActive = networkFilterActive,
                    onNetworkFilterChange = { networkFilterActive = it },
                    lifecycleListenerActive = lifecycleListenerActive,
                    onLifecycleListenerChange = { lifecycleListenerActive = it },
                    attachmentsProviderActive = attachmentsProviderActive,
                    onAttachmentsProviderChange = { attachmentsProviderActive = it },
                    onBackClick = { currentScreen = Screen.Main }
                )
                Screen.LoggingTests -> LoggingTestsScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.CrashProbe -> CrashProbeScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.AdvancedNetwork -> AdvancedNetworkScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.UserIdentifierTests -> UserIdentifierTestsScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.WebSocket -> WebSocketScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.Attributes -> AttributesScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.ReportFilters -> ReportFiltersScreen(
                    preFilterActive = preFilterActive,
                    onPreFilterChange = { preFilterActive = it },
                    postFilterActive = postFilterActive,
                    onPostFilterChange = { postFilterActive = it },
                    onBackClick = { currentScreen = Screen.Main }
                )
                Screen.ExtendedReports -> ExtendedReportsScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.CrashSignals -> CrashSignalsScreen(onBackClick = { currentScreen = Screen.Main })
                Screen.Appearance -> AppearanceScreen(onBackClick = { currentScreen = Screen.Main })
            }
        }
    }
}

@Composable
private fun DrawerContent(onAction: () -> Unit) {
    // What the last button did. Buttons that open an SDK screen close the menu; the others keep it
    // open so their result can be read here.
    var status by remember { mutableStateOf("") }
    fun setStatus(message: String) {
        status = message
        AppLogger.d(TAG, message)
    }

    ModalDrawerSheet {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Menu", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            if (status.isNotEmpty()) {
                Text(status, style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider()

            // Recording Control
            SectionLabel("Recording Control")
            DrawerButton("Resume") { Bugsee.resume(); setStatus("resume() called") }
            DrawerButton("Pause") { Bugsee.pause(); setStatus("pause() called") }
            DrawerButton("Stop") {
                Bugsee.stop()
                setStatus("stop() called. Once stopped, Delete Collected Data works and Launch starts Bugsee again.")
            }
            // Both pass the sample's options: without them the SDK would start with its defaults
            // and lose the sample's endpoint.
            DrawerButton("Launch") {
                Bugsee.launch(getPlatform().token, sampleLaunchOptions())
                setStatus("launch() called (options are ignored while Bugsee is running)")
            }
            DrawerButton("Relaunch") {
                Bugsee.relaunch(sampleLaunchOptions())
                setStatus("relaunch() called (refused unless Bugsee is running)")
            }
            DrawerButton("Is Launched?") { setStatus("isLaunched() = ${Bugsee.isLaunched()}") }
            HorizontalDivider()

            // Reports
            SectionLabel("Reports")
            DrawerButton("Show Report Dialog") { Bugsee.showReportDialog(); onAction() }
            DrawerButton("Show Filled Report Dialog") { Bugsee.showReportDialog("Test Summary", "Test Description", BugseeSeverity.Medium, listOf("qa")); onAction() }
            DrawerButton("Send Extended Report") {
                setStatus("Creating a report...")
                Bugsee.createReport { extendedReport ->
                    Bugsee.upload(extendedReport)
                    setStatus("Extended report created and queued for upload")
                }
            }
            HorizontalDivider()

            // Feedback
            SectionLabel("Feedback")
            DrawerButton("Show Feedback") { BugseeFeedback.show(); onAction() }
            DrawerButton("Set Feedback Greeting") { BugseeFeedback.setGreeting("Hello from KMP!"); setStatus("Feedback greeting set") }
            HorizontalDivider()

            // Exception
            SectionLabel("Exception")
            DrawerButton("Send Exception") {
                try { throw NullPointerException("Sample exception from KMP") } catch (ex: Exception) { Bugsee.logException(ex) }
                setStatus("NullPointerException logged")
            }
            HorizontalDivider()

            // Other
            SectionLabel("Other")
            DrawerButton("Capture View Hierarchy") { Bugsee.captureViewHierarchy(); setStatus("captureViewHierarchy() called") }
            // Both SDKs delete only while Bugsee is stopped.
            DrawerButton("Delete Collected Data (after Stop)") {
                setStatus("Deleting collected data...")
                Bugsee.deleteCollectedDataOnDevice { success ->
                    setStatus(if (success) "Collected data deleted" else "Not deleted: Stop Bugsee first")
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun DrawerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
fun MainScreen(
    onMenuClick: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Text("\u2630", style = MaterialTheme.typography.titleLarge)
            }
            Text("Bugsee KMP Sample", style = MaterialTheme.typography.titleMedium)
        }
        val content = @Composable {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BugseeProtect(modifier = Modifier.fillMaxSize()) {
                    CategoryButton("Exceptions") { onNavigate(Screen.Exceptions) }
                }
                Text(
                    "The Exceptions button is wrapped in BugseeProtect: it is hidden in the video.",
                    style = MaterialTheme.typography.bodySmall
                )
                CategoryButton("Filters & Listeners") { onNavigate(Screen.FiltersListeners) }
                CategoryButton("Report Filters") { onNavigate(Screen.ReportFilters) }
                CategoryButton("Attributes") { onNavigate(Screen.Attributes) }
                CategoryButton("Extended Reports") { onNavigate(Screen.ExtendedReports) }
                CategoryButton("Logging Tests") { onNavigate(Screen.LoggingTests) }
                CategoryButton("CrashProbe Tests") { onNavigate(Screen.CrashProbe) }
                CategoryButton("Crash Signals") { onNavigate(Screen.CrashSignals) }
                CategoryButton("Appearance") { onNavigate(Screen.Appearance) }
                CategoryButton("Network Tests") { onNavigate(Screen.NetworkTests) }
                CategoryButton("Advanced Network Tests") { onNavigate(Screen.AdvancedNetwork) }
                CategoryButton("User Identifier Tests") { onNavigate(Screen.UserIdentifierTests) }
                CategoryButton("WebSocket") { onNavigate(Screen.WebSocket) }
            }
        }

        content()
    }
}

@Composable
private fun CategoryButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}
