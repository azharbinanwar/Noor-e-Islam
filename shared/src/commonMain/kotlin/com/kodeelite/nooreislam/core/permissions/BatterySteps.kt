package com.kodeelite.nooreislam.core.permissions

import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.battery_step_continue
import com.kodeelite.nooreislam.resources.battery_step_pick_no_restrictions
import com.kodeelite.nooreislam.resources.battery_step_pick_unrestricted
import com.kodeelite.nooreislam.resources.battery_step_sleeping_apps
import com.kodeelite.nooreislam.resources.battery_step_turn_on_autostart
import com.kodeelite.nooreislam.resources.battery_step_turn_on_background
import com.kodeelite.nooreislam.resources.battery_step_turn_on_battery_saver
import org.jetbrains.compose.resources.StringResource

/**
 * The same switch hides behind a different label on every phone. Three short steps, worded the way her
 * own screen words them, beat one paragraph she will not read.
 */
/**
 * Phones whose own battery page opens instead of Android's dialog. The intent resolves on these too,
 * so nothing but the maker tells them apart, and skipping the steps there leaves her on a screen she
 * cannot read her way out of.
 */
fun needsBatterySteps(maker: String): Boolean =
    !(maker.startsWith("google") || maker.startsWith("samsung") || maker.startsWith("motorola") ||
            maker.startsWith("nokia") || maker.startsWith("sony") || maker.startsWith("asus") || maker.startsWith("lge"))

fun batterySteps(maker: String): List<StringResource> = listOf(Res.string.battery_step_continue) + when {
    maker.startsWith("vivo") || maker.startsWith("iqoo") ->
        listOf(Res.string.battery_step_turn_on_background, Res.string.battery_step_pick_unrestricted)

    maker.startsWith("xiaomi") || maker.startsWith("redmi") || maker.startsWith("poco") ->
        listOf(Res.string.battery_step_turn_on_battery_saver, Res.string.battery_step_pick_no_restrictions, Res.string.battery_step_turn_on_autostart)

    maker.startsWith("oppo") || maker.startsWith("realme") || maker.startsWith("oneplus") ->
        listOf(Res.string.battery_step_turn_on_background, Res.string.battery_step_turn_on_autostart)

    maker.startsWith("samsung") ->
        listOf(Res.string.battery_step_pick_unrestricted, Res.string.battery_step_sleeping_apps)

    else -> listOf(Res.string.battery_step_pick_unrestricted)
}
