package com.kodeelite.nooreislam.config.theme

import androidx.compose.runtime.Composable
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.dark
import com.kodeelite.nooreislam.resources.light
import com.kodeelite.nooreislam.resources.system
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** App appearance options. [dark] null = follow the system. */
enum class ThemeChoice(private val labelRes: StringResource, val dark: Boolean?) {
    Light(Res.string.light, dark = false),
    Dark(Res.string.dark, dark = true),
    System(Res.string.system, dark = null),
    ;

    val value: String get() = name

    @Composable
    fun label(): String = stringResource(labelRes)

    companion object {
        val default = System
        fun fromValue(value: String?) = entries.firstOrNull { it.value == value } ?: default
    }
}
