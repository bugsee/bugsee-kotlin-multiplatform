package com.bugsee.kmp.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeLaunchOptions
import com.bugsee.kmp.sample.Network.NetworkTestsScreen
import com.bugsee.kmp.sample.composeapp.generated.resources.Res
import com.bugsee.kmp.sample.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf("main") }
        
        // Launch Bugsee when the app starts
        LaunchedEffect(Unit) {
            val options = BugseeLaunchOptions()
            options.monitorNetwork = true
            options.captureLogs = true
            options.videoEnabled = true
            options.viewHierarchyEnabled = true
            options.shakeToReport = true
//            options.setCustomOption("endpoint", "https://apidev.bugsee.com/v2/")
            options.setCustomOption("debug", true)
            Bugsee.launch(getPlatform().token, options)

//            Bugsee.setLogFilter {
//                it?.message = "KMP"
//                println("level=" + it?.level)
//                return@setLogFilter it
//            }
        }
        
        when (currentScreen) {
            "main" -> {
                MainScreen(
                    onNetworkTestsClick = { currentScreen = "networkTests" }
                )
            }
            "networkTests" -> {
                NetworkTestsScreen(
                    onBackClick = { currentScreen = "main" }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    onNetworkTestsClick: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Network Tests button
        Button(onClick = onNetworkTestsClick) {
            Text("Network Tests")
        }
        
        // Original button
        Button(onClick = {
            showContent = !showContent
        }) {
            Text("Click me!")
        }
        
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
    }
}