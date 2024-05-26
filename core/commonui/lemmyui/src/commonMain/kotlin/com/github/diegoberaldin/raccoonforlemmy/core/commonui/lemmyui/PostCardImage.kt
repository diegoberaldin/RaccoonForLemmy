package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick

@Composable
fun PostCardImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
    autoLoadImages: Boolean = true,
    loadButtonContent: @Composable (() -> Unit)? = null,
    minHeight: Dp = 200.dp,
    maxHeight: Dp = Dp.Unspecified,
    blurred: Boolean = false,
    onImageClick: ((String) -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    if (imageUrl.isNotEmpty()) {
        CustomImage(
            modifier =
                modifier.fillMaxWidth()
                    .heightIn(min = minHeight, max = maxHeight)
                    .onClick(
                        onClick = { onImageClick?.invoke(imageUrl) },
                        onDoubleClick = onDoubleClick ?: {},
                        onLongClick = onLongClick ?: {},
                    ),
            url = imageUrl,
            quality = FilterQuality.Low,
            autoload = autoLoadImages,
            blurred = blurred,
            loadButtonContent = loadButtonContent,
            contentScale = ContentScale.FillWidth,
            onFailure = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = LocalXmlStrings.current.messageImageLoadingError,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            onLoading = { progress ->
                val prog =
                    if (progress != null) {
                        progress
                    } else {
                        val transition = rememberInfiniteTransition()
                        val res by transition.animateFloat(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec =
                                InfiniteRepeatableSpec(
                                    animation = tween(1000),
                                ),
                        )
                        res
                    }
                CircularProgressIndicator(
                    progress = { prog },
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        )
    }
}
