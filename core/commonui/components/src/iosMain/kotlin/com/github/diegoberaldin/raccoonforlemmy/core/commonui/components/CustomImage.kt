package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
actual fun CustomImage(
    modifier: Modifier,
    url: String,
    autoload: Boolean,
    loadButtonContent: @Composable (() -> Unit)?,
    contentDescription: String?,
    quality: FilterQuality,
    contentScale: ContentScale,
    dynamicallyAdjustScale: Boolean,
    alignment: Alignment,
    contentAlignment: Alignment,
    alpha: Float,
    colorFilter: ColorFilter?,
    onLoading: @Composable (BoxScope.(Float?) -> Unit)?,
    onFailure: @Composable (BoxScope.(Throwable) -> Unit)?,
) {
    var shouldBeRendered by remember(autoload) { mutableStateOf(autoload) }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (shouldBeRendered) {
            val painterResource = asyncPainterResource(
                data = url,
                filterQuality = quality,
            )
            KamelImage(
                modifier = Modifier.fillMaxSize(),
                resource = painterResource,
                contentDescription = contentDescription,
                contentScale = contentScale,
                alignment = alignment,
                contentAlignment = contentAlignment,
                alpha = alpha,
                colorFilter = colorFilter,
                onLoading = onLoading,
                onFailure = onFailure,
            )
        } else {
            Button(
                onClick = {
                    shouldBeRendered = true
                },
            ) {
                if (loadButtonContent != null) {
                    loadButtonContent()
                } else {
                    Text(
                        text = LocalXmlStrings.current.buttonLoad,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
