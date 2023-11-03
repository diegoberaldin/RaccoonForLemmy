package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.utils.findChildOfTypeRecursive
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

@Composable
internal fun MarkdownImage(content: String, node: ASTNode, autoLoadImages: Boolean = true) {
    val link =
        node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(content)
            ?.toString() ?: return

    CustomImage(
        url = link,
        autoload = autoLoadImages,
        quality = FilterQuality.Low,
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.fillMaxWidth(),
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
