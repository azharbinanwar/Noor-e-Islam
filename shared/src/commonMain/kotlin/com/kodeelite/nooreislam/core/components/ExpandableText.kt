package com.kodeelite.nooreislam.core.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.kodeelite.nooreislam.config.theme.AppTheme
import com.kodeelite.nooreislam.resources.Res
import com.kodeelite.nooreislam.resources.see_less
import com.kodeelite.nooreislam.resources.see_more
import org.jetbrains.compose.resources.stringResource

private val WHITESPACE = Regex("\\s+")

/**
 * Clamped text with an inline see-more/see-less toggle appended right at the truncation point
 * ("...text here See more"), not a link on its own line. Reuse wherever long text needs to fit a row.
 *
 * Two truncation modes, pick one:
 * - [collapsedMaxWords] set → word-count mode: cuts after N words, synchronous, no re-measure pass.
 * - [collapsedMaxWords] null (default) → line mode: measures once to find where [collapsedMaxLines]
 *   actually cuts the text, then swaps in a trimmed string with the toggle appended inline.
 */
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    collapsedMaxLines: Int = 2,
    collapsedMaxWords: Int? = null,
) {
    val colors = AppTheme.colors
    var expanded by remember(text) { mutableStateOf(false) }
    val moreLabel = stringResource(Res.string.see_more)
    val lessLabel = stringResource(Res.string.see_less)

    fun withToggle(body: String, label: String) = buildAnnotatedString {
        append(body)
        withLink(LinkAnnotation.Clickable(tag = "toggle", linkInteractionListener = { expanded = !expanded })) {
            withStyle(SpanStyle(color = colors.primary)) { append(" $label") }
        }
    }

    Column(modifier.animateContentSize()) {
        if (collapsedMaxWords != null) {
            val words = remember(text) { text.trim().split(WHITESPACE) }
            val overflows = words.size > collapsedMaxWords
            val displayed = when {
                expanded -> withToggle(text, lessLabel)
                overflows -> withToggle(words.take(collapsedMaxWords).joinToString(" ") + "…", moreLabel)
                else -> AnnotatedString(text)
            }
            Text(text = displayed, style = style.copy(color = color))
        } else {
            var clampedText by remember(text) { mutableStateOf<AnnotatedString?>(null) }
            val displayed = when {
                expanded -> withToggle(text, lessLabel)
                clampedText != null -> clampedText!!
                else -> AnnotatedString(text) // first pass: plain, used only to measure the cutoff
            }
            Text(
                text = displayed,
                style = style.copy(color = color),
                maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layout ->
                    if (!expanded && clampedText == null && layout.hasVisualOverflow) {
                        val cut = layout.getLineEnd(collapsedMaxLines - 1, visibleEnd = true)
                        val trimmed = text.substring(0, cut).dropLast(moreLabel.length + 3).trimEnd()
                        clampedText = withToggle("$trimmed…", moreLabel)
                    }
                },
            )
        }
    }
}
