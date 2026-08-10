package com.kodeelite.noorequran

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kodeelite.nooreislam.App

// Android caps the splash icon animation at 1000ms and cuts off anything past it (real devices
// enforce this; emulators don't). The animation lands at 900ms — hold just to the cap, no further.
private const val SPLASH_MIN_MS = 1000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val shownAt = SystemClock.uptimeMillis()
        installSplashScreen().setKeepOnScreenCondition {
            SystemClock.uptimeMillis() - shownAt < SPLASH_MIN_MS
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
private fun MainActivityPreview() {
    App()
}
