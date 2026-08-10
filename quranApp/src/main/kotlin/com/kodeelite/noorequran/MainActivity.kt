package com.kodeelite.noorequran

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kodeelite.nooreislam.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // the system splash is a flat green that hands straight over to QuranSplash, which draws
        // the animation itself — see QuranSplash.kt for why it isn't the system's animated icon
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

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
            }
        }
    }
}

@Preview
@Composable
private fun MainActivityPreview() {
    App()
}
