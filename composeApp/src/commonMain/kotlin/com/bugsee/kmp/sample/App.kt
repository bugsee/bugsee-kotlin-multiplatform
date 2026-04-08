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
import com.bugsee.kmp.BugseeLogLevel
import com.bugsee.kmp.BugseeSeverity
import com.bugsee.kmp.sample.screens.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

sealed class Screen {
    data object Main : Screen()
    data object NetworkTests : Screen()
    data object Exceptions : Screen()
    data object FiltersListeners : Screen()
    data object LoggingTests : Screen()
    data object CrashProbe : Screen()
    data object AdvancedNetwork : Screen()
    data object EmailTests : Screen()
    data object WebSocket : Screen()
    data object Attributes : Screen()
    data object ReportFilters : Screen()
    data object ExtendedReports : Screen()
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
                Screen.EmailTests -> EmailTestsScreen(onBackClick = { currentScreen = Screen.Main })
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
            }
        }
    }
}

@Composable
private fun DrawerContent(onAction: () -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Menu", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            HorizontalDivider()

            // Recording Control
            SectionLabel("Recording Control")
            DrawerButton("Resume") { Bugsee.resume(); onAction() }
            DrawerButton("Pause") { Bugsee.pause(); onAction() }
            DrawerButton("Stop") { Bugsee.stop(); onAction() }
            DrawerButton("Relaunch") { Bugsee.relaunch(); onAction() }
            DrawerButton("Is Launched?") { val l = Bugsee.isLaunched(); AppLogger.d(TAG, "isLaunched = $l"); Bugsee.log("isLaunched = $l", BugseeLogLevel.Info); onAction() }
            HorizontalDivider()

            // Reports
            SectionLabel("Reports")
            DrawerButton("Show Report Dialog") { Bugsee.showReportDialog(); onAction() }
            DrawerButton("Show Filled Report Dialog") { Bugsee.showReportDialog("Test Summary", "Test Description", BugseeSeverity.Medium, listOf("qa")); onAction() }
            DrawerButton("Send Extended Report") { Bugsee.createReport { report -> Bugsee.upload(report) }; onAction() }
            HorizontalDivider()

            // Feedback
            SectionLabel("Feedback")
            DrawerButton("Show Feedback") { Bugsee.showFeedback(); onAction() }
            DrawerButton("Set Feedback Greeting") { Bugsee.setDefaultFeedbackGreeting("Hello from KMP!"); onAction() }
            HorizontalDivider()

            // Logging
            SectionLabel("Exception")
            DrawerButton("Send Exception") {
                try { throw NullPointerException("Sample exception from KMP") } catch (ex: Exception) { Bugsee.logException(ex) }
                onAction()
            }

            // Other
            SectionLabel("Other")
            DrawerButton("Capture View Hierarchy") { Bugsee.captureViewHierarchy(); onAction() }
            DrawerButton("Delete Collected Data") { Bugsee.deleteCollectedDataOnDevice { success -> Bugsee.log("Data deleted: $success", BugseeLogLevel.Info) }; onAction() }
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
                CategoryButton("Filters & Listeners") { onNavigate(Screen.FiltersListeners) }
                CategoryButton("Report Filters") { onNavigate(Screen.ReportFilters) }
                CategoryButton("Attributes") { onNavigate(Screen.Attributes) }
                CategoryButton("Extended Reports") { onNavigate(Screen.ExtendedReports) }
                CategoryButton("Logging Tests") { onNavigate(Screen.LoggingTests) }
                CategoryButton("CrashProbe Tests") { onNavigate(Screen.CrashProbe) }
                CategoryButton("Modern Network Tests") { onNavigate(Screen.NetworkTests) }
                CategoryButton("Advanced Network Tests") { onNavigate(Screen.AdvancedNetwork) }
                CategoryButton("Email Tests") { onNavigate(Screen.EmailTests) }
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
