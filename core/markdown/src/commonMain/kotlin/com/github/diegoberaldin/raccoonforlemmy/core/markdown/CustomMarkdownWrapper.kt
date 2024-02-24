package com.github.diegoberaldin.raccoonforlemmy.core.markdown

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.isUnspecified
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownParagraph
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownPadding
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.markdownColor
import com.mikepenz.markdown.model.markdownPadding
import com.mikepenz.markdown.model.markdownTypography

private val String.containsSpoiler: Boolean
    get() = SpoilerRegex.spoilerOpening.containsMatchIn(this)

private val String.isImage: Boolean
    get() = ImageRegex.image.matches(this)

@Composable
fun CustomMarkdownWrapper(
    content: String,
    modifier: Modifier,
    colors: MarkdownColors = markdownColor(),
    typography: MarkdownTypography = markdownTypography(),
    padding: MarkdownPadding = markdownPadding(),
    autoLoadImages: Boolean,
    maxLines: Int? = null,
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
            val substring = model.content.substring(
                startIndex = model.node.startOffset,
                endIndex = model.node.endOffset,
            )
            when {
                substring.containsSpoiler -> {
                    CustomMarkdownSpoiler(content = substring)
                }

                substring.isImage -> {
                    val res = ImageRegex.image.find(substring)
                    val link = res?.groups?.get("url")?.value.orEmpty()
                    CustomMarkdownImage(
                        url = link,
                        autoLoadImages = autoLoadImages,
                        onOpenImage = onOpenImage,
                    )
                }

                else -> {
                    MarkdownParagraph(
                        modifier = if (maxLines != null && maxLines > 0) {
                            val maxHeightPx = with(LocalDensity.current) {
                                val lineHeight =
                                    LocalMarkdownTypography.current.paragraph.lineHeight
                                val base = if (lineHeight.isUnspecified) {
                                    LocalMarkdownTypography.current.paragraph.fontSize.roundToPx()
                                } else {
                                    lineHeight.roundToPx()
                                }
                                base * maxLines
                            }
                            val maxHeightDp = with(LocalDensity.current) {
                                maxHeightPx.toDp()
                            }
                            Modifier.heightIn(max = maxHeightDp)
                        } else {
                            Modifier
                        },
                        content = model.content,
                        node = model.node
                    )
                }
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
        LocalUriHandler provides customUriHandler,
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = LocalDensity.current.fontScale * 0.99f,
        ),
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
