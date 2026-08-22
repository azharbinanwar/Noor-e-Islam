package com.kodeelite.nooreislam.core.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * A calendar holding a single row of days — the week view, as opposed to the month grid.
 * Lucide has no such icon, so this is drawn to its rules: 24-unit grid, stroke 2, round caps.
 *
 * Two dots rather than three, and fatter than the tracker's: at 18dp three thin ones silt up.
 */
val WeekIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "WeekIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        val stroke = SolidColor(Color.Black)
        // frame + the two tabs + the header rule, exactly Lucide's calendar base
        path(
            stroke = stroke,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(5f, 4f)
            horizontalLineTo(19f)
            arcToRelative(2f, 2f, 0f, false, true, 2f, 2f)
            verticalLineTo(20f)
            arcToRelative(2f, 2f, 0f, false, true, -2f, 2f)
            horizontalLineTo(5f)
            arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
            verticalLineTo(6f)
            arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
            close()

            moveTo(16f, 2f); verticalLineToRelative(4f)
            moveTo(8f, 2f); verticalLineToRelative(4f)
            moveTo(3f, 10f); horizontalLineToRelative(18f)
        }
        // the week itself
        path(fill = SolidColor(Color.Black)) {
            moveTo(9.6f, 15.5f)
            arcToRelative(1.6f, 1.6f, 0f, true, true, -3.2f, 0f)
            arcToRelative(1.6f, 1.6f, 0f, true, true, 3.2f, 0f)
            close()
            moveTo(17.6f, 15.5f)
            arcToRelative(1.6f, 1.6f, 0f, true, true, -3.2f, 0f)
            arcToRelative(1.6f, 1.6f, 0f, true, true, 3.2f, 0f)
            close()
        }
    }.build()
}
