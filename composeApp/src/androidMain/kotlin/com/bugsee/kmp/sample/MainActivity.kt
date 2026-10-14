package com.bugsee.kmp.sample

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeAttachment
import com.bugsee.kmp.BugseeLaunchOptions
import com.bugsee.kmp.sample.composeapp.generated.resources.Res
import io.ktor.utils.io.core.toByteArray


class KMPSample: Application() {
    override fun onCreate() {
        super.onCreate()

        val options = BugseeLaunchOptions()
        options.monitorNetwork = true
        options.captureLogs = true
        options.videoEnabled = true
        options.viewHierarchyEnabled = true
        options.shakeToReport = true
        options.reportLabelsEnabled = true
        options.reportLabelsRequired = false
        // Internal Android SDK 7 keys; the 6.x "endpoint" / "debug" keys are ignored.
        options.setCustomOption("com.bugsee.option.\$\$ENDPOINT", getPlatform().appdevEndpoint)
        options.setCustomOption("com.bugsee.option.\$\$DEBUG", true)
        Bugsee.launch(getPlatform().token, options)

        Bugsee.setOnNewFeedbackListener { messages ->
            messages.forEach { message ->
                AppLogger.d("KMPSample", "New feedback: $message")
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

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