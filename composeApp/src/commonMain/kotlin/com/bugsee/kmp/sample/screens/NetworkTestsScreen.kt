package com.bugsee.kmp.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun NetworkTestsScreen(
    onBackClick: () -> Unit
) {
    var networkResult by remember { mutableStateOf<String?>(null) }
    var isLoadingKtor by remember { mutableStateOf(false) }
    var loadingButton by remember { mutableStateOf<String?>(null) }
    val networkHelper = remember { NetworkHelper() }
    val coroutineScope = rememberCoroutineScope()
    
    val testUrl = "https://dummyjson.com/test"

    TestScreenScaffold(title = "Exceptions", onBackClick = onBackClick) {

        
        // Row 1: Ktor test button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    isLoadingKtor = true
                    loadingButton = "Ktor"
                    networkResult = null
                    coroutineScope.launch {
                        val result = networkHelper.ktorGetRequest(testUrl)
                        isLoadingKtor = false
                        loadingButton = null
                        networkResult = when (result) {
                            is NetworkResult.Success -> "Ktor Success: ${result.statusCode}\n${result.body}"
                            is NetworkResult.Error -> "Ktor Error: ${result.message}"
                        }
                    }
                },
                enabled = !isLoadingKtor
            ) {
                if (isLoadingKtor && loadingButton == "Ktor") {
                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp).height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoadingKtor && loadingButton == "Ktor") "Loading..." else "Ktor test")
            }
        }

        // Display network result
        networkResult?.let { result ->
            Text(
                text = result,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
