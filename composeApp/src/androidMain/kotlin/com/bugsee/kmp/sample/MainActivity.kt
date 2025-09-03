package com.bugsee.kmp.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

//        val options = BugseeLaunchOptionsAndroid()
//        options.captureLogs = true
//        options.videoEnabled = true
//        options.viewHierarchyEnabled = true
//        options.shakeToReport = true
//        options.screenshotToReport = true
////        options.setCustomOption("endpoint", "https://apidev.bugsee.com/v2/")
//        Bugsee.launch(getPlatform().token, options)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}