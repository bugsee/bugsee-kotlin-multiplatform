package com.bugsee.kmp.sample

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeLaunchOptions
import com.bugsee.kmp.feedback.BugseeFeedback
import com.bugsee.kmp.feedback.BugseeFeedbackListener


class KMPSample: Application() {
    override fun onCreate() {
        super.onCreate()

        val options = BugseeLaunchOptions()
        options.captureNetwork = true
        options.captureLogs = true
        options.captureVideo = true
        options.captureViewHierarchy = true
        options.reportingTriggerByShake = true
        options.reportingUILabelsEnabled = true
        options.reportingUILabelsRequired = false
        // Internal Android SDK 7 keys; the 6.x "endpoint" / "debug" keys are ignored.
        options.setCustomOption("com.bugsee.option.\$\$ENDPOINT", getPlatform().appdevEndpoint)
        options.setCustomOption("com.bugsee.option.\$\$DEBUG", true)
        Bugsee.launch(getPlatform().token, options)

        BugseeFeedback.setListener(object : BugseeFeedbackListener {
            override fun onNewMessagesReceived(messages: List<String>) {
                messages.forEach { message ->
                    AppLogger.d("KMPSample", "New feedback: $message")
                }
            }

            override fun onNewMessageSent(message: String) {
                AppLogger.d("KMPSample", "Feedback sent: $message")
            }
        })
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