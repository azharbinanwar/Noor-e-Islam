package com.example.miqatapp.feature.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent

// Prayer Tile (design 19f) — 2×2. Current Miqat icon + name (small, above) + the current prayer's end time (big).
class PrayerTileWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val style = WidgetConfig.claim(styleId(context, id))
        val rv = loadSnapshot()?.let { tileRemoteViews(context, it, style) }
        provideContent { if (rv != null) SquareRemoteViews(rv) }
    }
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { SquareRemoteViews(tileRemoteViews(context, sampleSnapshot(), WidgetStyle())) }
    }
}

class PrayerTileWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerTileWidget()
}

internal fun tileRemoteViews(ctx: Context, snap: WidgetSnapshot, style: WidgetStyle): RemoteViews {
    val head = headState(snap, System.currentTimeMillis())
    val color = style.color
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_tile_widget", "layout"))
    widgetChrome(rv, ctx, color, style.alpha)
    for (v in listOf("name", "time")) rv.setTextColor(viewId(ctx, v), on)
    rv.setImageViewResource(viewId(ctx, "icon"), badgeIconRes(ctx, head.current.key))
    rv.setInt(viewId(ctx, "icon"), "setColorFilter", on)
    rv.setTextViewText(viewId(ctx, "name"), head.current.label.uppercase())
    rv.setTextViewText(viewId(ctx, "time"), head.next.timeText.substringBefore(' ')) // current prayer's end = next slot's start
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
