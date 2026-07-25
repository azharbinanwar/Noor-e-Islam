package com.kodeelite.nooreislam.feature.widget

import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.fillMaxSize

// Dark prayer card (design 8b), native RemoteViews. Current prayer + live countdown + app-icon badge,
// and the other four times across the bottom. New widget — old ones untouched.
class PrayerCardWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val style = WidgetConfig.claim(styleId(context, id))
        val rv = loadSnapshot()?.let { prayerCardRemoteViews(context, it, live = true, style) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val rv = prayerCardRemoteViews(context, sampleSnapshot(), live = false, WidgetStyle())
        provideContent { AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
}

class PrayerCardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerCardWidget()
}

internal fun prayerCardRemoteViews(ctx: Context, snap: WidgetSnapshot, live: Boolean, style: WidgetStyle): RemoteViews {
    val now = System.currentTimeMillis()
    val head = headState(snap, now) // header = the real current segment (Fajr → Sunrise/Ishraq → Dhuhr …)
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_card_widget", "layout"))
    val color = style.color
    val on = color.on.toArgb() // one text/icon colour for the whole card

    // Background = the picked colour's gradient, faded by the chosen opacity (reveals wallpaper).
    rv.setImageViewBitmap(viewId(ctx, "bg"), gradientBitmap(ctx, 360, 150, color.fill.toArgb(), color.fillEnd.toArgb()))
    rv.setInt(viewId(ctx, "bg"), "setImageAlpha", (style.alpha * 255).toInt())
    rv.setInt(viewId(ctx, "divider"), "setBackgroundColor", (on and 0x00FFFFFF) or 0x24000000)  // on @ ~14%
    rv.setImageViewBitmap(viewId(ctx, "badgebg"), roundedRectBitmap(ctx, 50, 15f, (on and 0x00FFFFFF) or 0x1F000000)) // rounded, on @ ~12%
    rv.setInt(viewId(ctx, "badgeicon"), "setColorFilter", on)
    rv.setInt(viewId(ctx, "watermark"), "setColorFilter", on)
    for (v in listOf("label", "name", "nextinfo", "chrono", "chronoStatic")) rv.setTextColor(viewId(ctx, v), on)

    rv.setTextViewText(viewId(ctx, "name"), head.current.label)
    rv.setTextViewText(viewId(ctx, "nextinfo"), "Next · ${head.next.label} · ")
    rv.setImageViewResource(viewId(ctx, "badgeicon"), badgeIconRes(ctx, head.current.key))

    val chrono = viewId(ctx, "chrono")
    val chronoStatic = viewId(ctx, "chronoStatic")
    if (live) {
        rv.setViewVisibility(chrono, View.VISIBLE)
        rv.setViewVisibility(chronoStatic, View.GONE)
        val base = SystemClock.elapsedRealtime() + (head.next.atMillis - now)
        rv.setChronometerCountDown(chrono, true)
        rv.setChronometer(chrono, base, "in %s", true)
    } else {
        rv.setViewVisibility(chrono, View.GONE)
        rv.setViewVisibility(chronoStatic, View.VISIBLE)
        rv.setTextViewText(chronoStatic, countdownLabel(head.next.atMillis - now))
    }

    // Footer = all five prayers, one colour throughout.
    snap.prayers.take(5).forEachIndexed { i, p ->
        rv.setTextViewText(viewId(ctx, "t${i}name"), p.label)
        rv.setTextColor(viewId(ctx, "t${i}name"), on)
        rv.setTextViewText(viewId(ctx, "t${i}time"), p.timeText)
        rv.setTextColor(viewId(ctx, "t${i}time"), on)
    }

    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
