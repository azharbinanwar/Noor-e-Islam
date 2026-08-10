package com.kodeelite.nooreislam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kodeelite.nooreislam.core.focus.PhoneSilencer
import com.kodeelite.nooreislam.feature.notifications.scheduler.NotificationScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        PhoneSilencer.restoreIfStuck() // un-mute if a killed service never restored the ringer
        PhoneSilencer.rescheduleAll()  // re-arm prayer alarms (times may have rolled to a new day)
        NotificationScheduler.rebuildAsync() // app-open refill: top up the notification window
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}