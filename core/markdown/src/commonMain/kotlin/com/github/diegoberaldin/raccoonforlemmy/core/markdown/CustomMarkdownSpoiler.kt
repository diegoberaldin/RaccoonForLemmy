package com.github.diegoberaldin.raccoonforlemmy.core.markdown

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.mikepenz.markdown.compose.elements.MarkdownText

@Composable
internal fun CustomMarkdownSpoiler(
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        var lastIndex = 0
        val matches = SpoilerRegex.spoilerOpening.findAll(content)
        for (match in matches) {
            val openingStart = match.range.first
            val openingEnd = match.range.last
            if (openingStart > lastIndex) {
                val subcontent = content.substring(
                    startIndex = lastIndex,
                    endIndex = openingStart,
                )
                MarkdownText(content = subcontent)
            }
            val spoilerTitle = match.groups["title"]?.value.orEmpty()
            val closeMatch = SpoilerRegex.spoilerClosing.find(
                input = content,
                startIndex = openingEnd,
            )
            val spoilerContent = closeMatch?.let {
                content.substring(
                    startIndex = openingEnd + 1,
                    endIndex = it.range.last - 3,
                )
            } ?: content.substring(
                startIndex = openingEnd + 1,
            )

            InnerSpoilerElement(
                title = spoilerTitle,
                content = spoilerContent,
            )

            lastIndex = closeMatch?.range?.last ?: content.lastIndex
        }
        if (lastIndex < content.lastIndex) {
            val subcontent = content.substring(
                startIndex = lastIndex,
            )
            MarkdownText(content = subcontent)
        }
    }
}

@Composable
private fun InnerSpoilerElement(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
    ) {
        if (!expanded) {
            MarkdownText(
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick(onClick = { expanded = !expanded }),
                content = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 20.sp)) {
                        append("▶︎ ")
                    }
                    append(title)
                },
            )
        } else {
            MarkdownText(
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick(onClick = { expanded = !expanded }),
                content = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 20.sp)) {
                        append("▼︎ ")
                    }
                    append(title)
                },
            )
            MarkdownText(
                modifier = Modifier.padding(
                    start = 18.dp,
                    bottom = 10.dp,
                ),
                content = content,
            )
        }
    }
}
