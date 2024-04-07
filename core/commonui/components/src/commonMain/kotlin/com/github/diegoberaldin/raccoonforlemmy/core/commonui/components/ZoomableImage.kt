package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

private const val LOADING_ANIMATION_DURATION = 1000

@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    url: String,
    autoLoadImages: Boolean = false,
) {
    var scale by remember {
        mutableStateOf(1f)
    }
    var offset by remember {
        mutableStateOf(Offset.Zero)
    }

    BoxWithConstraints(
        modifier = modifier
    ) {
        val transformableState =
            rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 16f)

                val extraWidth = (scale - 1) * constraints.maxWidth
                val extraHeight = (scale - 1) * constraints.maxHeight

                val maxX = extraWidth / 2
                val maxY = extraHeight / 2

                offset = Offset(
                    x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
                    y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY),
                )
            }

        CustomImage(
            modifier = Modifier
                .clip(RectangleShape)
                .fillMaxSize()
                .background(Color.Black)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                )
                .transformable(transformableState),
            url = url,
            contentScale = ContentScale.FillWidth,
            quality = FilterQuality.High,
            autoload = autoLoadImages,
            onFailure = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = LocalXmlStrings.current.messageImageLoadingError,
                    color = MaterialTheme.colorScheme.onBackground,
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
                            animation = tween(LOADING_ANIMATION_DURATION)
                        )
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
