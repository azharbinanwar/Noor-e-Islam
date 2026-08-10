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

private const val SPLASH_MIN_MS = 1400L // the animation runs ~1.2s; hold so it isn't cut short

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
