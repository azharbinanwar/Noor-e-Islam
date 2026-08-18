package com.kodeelite.noorequran

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kodeelite.nooreislam.App
import com.kodeelite.nooreislam.core.navigation.NOTIF_ROUTE_KEY
import com.kodeelite.nooreislam.core.navigation.PendingNavigation
import com.kodeelite.nooreislam.feature.notifications.scheduler.NotificationScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // the system splash is a flat green that hands straight over to QuranSplash, which draws
        // the animation itself — see QuranSplash.kt for why it isn't the system's animated icon
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        PendingNavigation.offer(intent?.getStringExtra(NOTIF_ROUTE_KEY))

        setContent {
            var splashDone by remember { mutableStateOf(false) }
            var appStarted by remember { mutableStateOf(false) }
            // First frame draws only the splash canvas, so the green system window hands over almost
            // immediately. App() is heavy to compose — it joins from the second frame, building
            // underneath while the animation is already playing instead of delaying it.
            LaunchedEffect(Unit) { appStarted = true }
            Box(Modifier.fillMaxSize()) {
                if (appStarted) App()
                if (!splashDone) QuranSplash(onFinished = { splashDone = true })
                if (BuildConfig.DEBUG) {
                    BasicText(
                        text = "DEBUG",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 40.dp, y = 24.dp)
                            .rotate(45f)
                            .background(Color(0xFFD32F2F))
                            .width(140.dp)
                            .padding(vertical = 3.dp),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }

    // Tapped while the app was already running — onCreate doesn't run again.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        PendingNavigation.offer(intent.getStringExtra(NOTIF_ROUTE_KEY))
    }

    override fun onResume() {
        super.onResume()
        NotificationScheduler.rebuildAsync() // app-open refill: top up the notification window
    }
}

@Preview
@Composable
private fun MainActivityPreview() {
    App()
}
