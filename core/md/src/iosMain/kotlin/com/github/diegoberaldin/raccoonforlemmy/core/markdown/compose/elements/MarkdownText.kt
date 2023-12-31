package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownColors
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalReferenceLinkHandler
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.utils.TAG_IMAGE_URL
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.utils.TAG_URL
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun MarkdownText(
    content: String,
    maxLines: Int?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalMarkdownTypography.current.text,
    onOpenUrl: ((String) -> Unit)? = null,
    inlineImages: Boolean = true,
    autoLoadImages: Boolean = true,
    onOpenImage: ((String) -> Unit)? = null,
) {
    MarkdownText(
        content = AnnotatedString(content),
        maxLines = maxLines,
        modifier = modifier,
        style = style,
        onOpenUrl = onOpenUrl,
        inlineImages = inlineImages,
        autoLoadImages = autoLoadImages,
        onOpenImage = onOpenImage,
    )
}

@Composable
internal fun MarkdownText(
    content: AnnotatedString,
    maxLines: Int? = null,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalMarkdownTypography.current.text,
    onOpenUrl: ((String) -> Unit)? = null,
    inlineImages: Boolean = true,
    autoLoadImages: Boolean = true,
    onOpenImage: ((String) -> Unit)? = null,
) {
    val referenceLinkHandler = LocalReferenceLinkHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val hasUrl = content.getStringAnnotations(TAG_URL, 0, content.length).any()
    val textModifier = if (hasUrl) {
        modifier.pointerInput(Unit) {
            detectTapGestures { pos ->
                layoutResult.value?.let { layoutResult ->
                    val position = layoutResult.getOffsetForPosition(pos)
                    content.getStringAnnotations(TAG_URL, position, position)
                        .firstOrNull()
                        ?.let {
                            val url = referenceLinkHandler.find(it.item)
                            onOpenUrl?.invoke(url)
                        }
                }
            }
        }
    } else {
        modifier
    }

    var imageUrl by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (inlineImages || content.text != imageUrl) {
            Text(
                maxLines = maxLines ?: Int.MAX_VALUE,
                text = content,
                modifier = textModifier,
                style = style,
                inlineContent = mapOf(
                    TAG_IMAGE_URL to InlineTextContent(
                        if (inlineImages) {
                            Placeholder(
                                180.sp,
                                180.sp,
                                PlaceholderVerticalAlign.Bottom,
                            ) // TODO: identify flexible scaling!
                        } else {
                            Placeholder(1.sp, 1.sp, PlaceholderVerticalAlign.Bottom)
                        }
                    ) { link ->
                        if (inlineImages) {
                            CustomImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onClick(
                                        onClick = { onOpenImage?.invoke(imageUrl) },
                                    ),
                                url = link,
                                autoload = autoLoadImages,
                                quality = FilterQuality.Low,
                                contentScale = ContentScale.FillWidth,
                                onFailure = {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        text = stringResource(MR.strings.message_image_loading_error),
                                        style = LocalMarkdownTypography.current.text
                                    )
                                },
                                onLoading = { progress ->
                                    val prog = if (progress != null) {
                                        progress
                                    } else {
                                        val transition = rememberInfiniteTransition()
                                        val res by transition.animateFloat(
                                            initialValue = 0f,
                                            targetValue = 1f,
                                            animationSpec = InfiniteRepeatableSpec(
                                                animation = tween(1000)
                                            )
                                        )
                                        res
                                    }
                                    CircularProgressIndicator(
                                        progress = prog,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                },
                            )
                        } else {
                            imageUrl = link
                        }
                    },
                ),
                color = LocalMarkdownColors.current.text,
                onTextLayout = { layoutResult.value = it },
            )
        }
        if (!inlineImages && imageUrl.isNotEmpty()) {
            CustomImage(
                modifier = modifier.fillMaxWidth()
                    // TODO: improve fixed values
                    .heightIn(min = 200.dp, max = Dp.Unspecified)
                    .clip(RoundedCornerShape(20.dp))
                    .onClick(
                        onClick = { onOpenImage?.invoke(imageUrl) },
                        onDoubleClick = {},
                    ),
                url = imageUrl,
                autoload = autoLoadImages,
                quality = FilterQuality.Low,
                contentScale = ContentScale.FillWidth,
                onFailure = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = stringResource(MR.strings.message_image_loading_error),
                        style = LocalMarkdownTypography.current.text,
                    )
                },
                onLoading = { progress ->
                    val prog = if (progress != null) {
                        progress
                    } else {
                        val transition = rememberInfiniteTransition()
                        val res by transition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = InfiniteRepeatableSpec(
                                animation = tween(1000)
                            )
                        )
                        res
                    }
                    CircularProgressIndicator(
                        progress = prog,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }
}
