package com.github.diegoberaldin.raccoonforlemmy.core.markdown

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownParagraph
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownPadding
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.markdownPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor

private val String.containsSpoiler: Boolean
    get() = SpoilerRegex.spoilerOpening.containsMatchIn(this)

private val String.isImage: Boolean
    get() = ImageRegex.image.matches(this)

private const val MAX_LINES_SCALE_FACTOR = 0.97f
private const val GLOBAL_SCALE_FACTOR = 0.99f
private const val LINK_DELAY = 250L

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
    val maxHeightDp =
        with(LocalDensity.current) {
            if (maxLines == null) {
                Dp.Unspecified
            } else {
                val lineHeight =
                    typography.paragraph.lineHeight
                val base =
                    if (lineHeight.isUnspecified) {
                        floor(typography.paragraph.fontSize.toPx() * MAX_LINES_SCALE_FACTOR)
                    } else {
                        lineHeight.toPx() * MAX_LINES_SCALE_FACTOR
                    }
                (base * maxLines).toDp()
            }
        }
    val scope = rememberCoroutineScope()
    var isOpeningUrl by remember { mutableStateOf(false) }
    val customUriHandler =
        remember {
            object : UriHandler {
                override fun openUri(uri: String) {
                    isOpeningUrl = true
                    onOpenUrl?.invoke(uri)
                    scope.launch {
                        delay(LINK_DELAY)
                        isOpeningUrl = false
                    }
                }
            }
        }
    val components =
        markdownComponents(
            paragraph = { model ->
                val substring =
                    model.content.substring(
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
                            content = model.content,
                            node = model.node,
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
        LocalDensity provides
            Density(
                density = LocalDensity.current.density,
                fontScale = LocalDensity.current.fontScale * GLOBAL_SCALE_FACTOR,
            ),
    ) {
        Markdown(
            modifier =
                modifier
                    .heightIn(min = 0.dp, max = maxHeightDp)
                    .onClick(
                        onClick = {
                            if (!isOpeningUrl) {
                                onClick?.invoke()
                            }
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
