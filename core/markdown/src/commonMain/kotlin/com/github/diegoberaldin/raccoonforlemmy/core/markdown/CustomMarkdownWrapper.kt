package com.github.diegoberaldin.raccoonforlemmy.core.markdown

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownParagraph
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownPadding
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.markdownColor
import com.mikepenz.markdown.model.markdownPadding
import com.mikepenz.markdown.model.markdownTypography

private val spoilerRegex = Regex(":::\\s+spoiler\\s+(?<title>.*?)\\n(?<content>.*?)\\n\\s*?:::")

@Composable
fun CustomMarkdownWrapper(
    content: String,
    modifier: Modifier,
    colors: MarkdownColors = markdownColor(),
    typography: MarkdownTypography = markdownTypography(),
    padding: MarkdownPadding = markdownPadding(),
    autoLoadImages: Boolean,
    maxLines: Int? = null,
    // TODO
    inlineImages: Boolean = true,
    onOpenUrl: ((String) -> Unit)?,
    onOpenImage: ((String) -> Unit)?,
    onClick: (() -> Unit)?,
    onDoubleClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    val customUriHandler = remember {
        object : UriHandler {
            override fun openUri(uri: String) {
                onOpenUrl?.invoke(uri)
            }
        }
    }
    val components = markdownComponents(
        paragraph = { model ->
            val substring = model.content.substring(model.node.startOffset..<model.node.endOffset)
            val match = spoilerRegex.findAll(substring).firstOrNull()
            if (match != null) {
                val spoilerTitle = match.groups["title"]?.value.orEmpty()
                val spoilerContent = match.groups["content"]?.value.orEmpty()
                var expanded by remember { mutableStateOf(false) }
                if (!expanded) {
                    MarkdownText(
                        modifier = Modifier.onClick(onClick = { expanded = !expanded }),
                        content = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 20.sp)) {
                                append("▶︎ ")
                            }
                            append(spoilerTitle)
                        },
                    )
                } else {
                    MarkdownText(
                        modifier = Modifier.onClick(onClick = { expanded = !expanded }),
                        content = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 20.sp)) {
                                append("▼︎ ")
                            }
                            append(spoilerTitle)
                        },
                    )
                    MarkdownText(
                        content = spoilerContent,
                    )
                }
            } else {
                MarkdownParagraph(
                    modifier = if (maxLines != null) {
                        val maxHeightSp =
                            LocalMarkdownTypography.current.paragraph.lineHeight * maxLines
                        val maxHeightDp = with(LocalDensity.current) {
                            maxHeightSp.toDp()
                        }
                        Modifier.heightIn(max = maxHeightDp)
                    } else {
                        Modifier
                    },
                    content = model.content,
                    node = model.node
                )
            }
        },
        image = { model ->
            CustomMarkdownImage(
                node = model.node,
                content = content,
                onOpenImage = onOpenImage,
                autoLoadImages = autoLoadImages,
            )
        },
    )

    CompositionLocalProvider(
        LocalUriHandler provides customUriHandler
    ) {
        Markdown(
            modifier = modifier.onClick(
                onClick = {
                    onClick?.invoke()
                },
                onLongClick = {
                    onLongClick?.invoke()
                },
                onDoubleClick = {
                    onDoubleClick?.invoke()
                },
            ),
            content = content.sanitize(),
            colors = colors,
            typography = typography,
            padding = padding,
            components = components,
        )
    }
}
