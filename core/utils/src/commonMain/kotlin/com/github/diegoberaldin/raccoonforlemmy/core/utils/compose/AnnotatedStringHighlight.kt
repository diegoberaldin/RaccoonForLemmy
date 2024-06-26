package com.github.diegoberaldin.raccoonforlemmy.core.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun buildAnnotatedStringWithHighlights(
    text: String,
    highlightText: String?,
    highlightColor: Color,
): AnnotatedString {
    if (highlightText == null) {
        return buildAnnotatedString {
            append(text)
        }
    }

    return buildAnnotatedString {
        var start = 0
        while (text.indexOf(highlightText, start, ignoreCase = true) != -1 && highlightText.isNotBlank()) {
            val firstIndex = text.indexOf(highlightText, start, true)
            val end = firstIndex + highlightText.length
            append(text.substring(start, firstIndex))
            withStyle(style = SpanStyle(background = highlightColor)) {
                append(text.substring(firstIndex, end))
            }
            start = end
        }
        append(text.substring(start, text.length))
        toAnnotatedString()
    }
}