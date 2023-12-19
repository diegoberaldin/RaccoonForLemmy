package com.github.diegoberaldin.raccoonforlemmy.core.markdown.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
interface MarkdownColors {
    /** Represents the color used for the text of this [Markdown] component. */
    val text: Color

    /** Represents the background color for this [Markdown] component. */
    val backgroundCode: Color

    /** Represents the linl color for this [Markdown] component. */
    val linkColor: Color
}

@Immutable
private class DefaultMarkdownColors(
    override val text: Color,
    override val backgroundCode: Color,
    override val linkColor: Color,
) : MarkdownColors

@Composable
fun markdownColor(
    text: Color = MaterialTheme.colorScheme.onBackground,
    backgroundCode: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
    linkColor: Color = MaterialTheme.colorScheme.primary,
): MarkdownColors = DefaultMarkdownColors(
    text = text,
    backgroundCode = backgroundCode,
    linkColor = linkColor,
)
