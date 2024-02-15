package com.github.diegoberaldin.raccoonforlemmy.core.markdown

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
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

@Composable
internal fun CustomMarkdownImage(
    node: ASTNode,
    content: String,
    onOpenImage: ((String) -> Unit)?,
    autoLoadImages: Boolean,
) {
    val link = node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)
        ?.getTextInNode(content)
        ?.toString().orEmpty()
    if (link.isNotEmpty()) {
        CustomImage(
            modifier = Modifier
                .fillMaxWidth()
                .onClick(
                    onClick = {
                        onOpenImage?.invoke(link)
                    },
                ),
            url = link,
            autoload = autoLoadImages,
            quality = FilterQuality.Low,
            contentScale = ContentScale.FillWidth,
            onFailure = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = LocalXmlStrings.current.messageImageLoadingError,
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
                        ),
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
