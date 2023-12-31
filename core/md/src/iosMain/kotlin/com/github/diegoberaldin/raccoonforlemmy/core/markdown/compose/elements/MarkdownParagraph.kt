package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownColors
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.utils.buildMarkdownAnnotatedString
import org.intellij.markdown.ast.ASTNode

@Composable
internal fun MarkdownParagraph(
    content: String,
    node: ASTNode,
    maxLines: Int? = null,
    style: TextStyle = LocalMarkdownTypography.current.paragraph,
    onOpenUrl: ((String) -> Unit)? = null,
    inlineImages: Boolean = true,
    autoLoadImages: Boolean = true,
    onOpenImage: ((String) -> Unit)? = null,
) {
    val styledText = buildAnnotatedString {
        pushStyle(style.toSpanStyle())
        buildMarkdownAnnotatedString(
            content = content,
            node = node,
            linkColor = LocalMarkdownColors.current.linkColor
        )
        pop()
    }
    MarkdownText(
        content = styledText,
        maxLines = maxLines,
        style = style,
        onOpenUrl = onOpenUrl,
        inlineImages = inlineImages,
        onOpenImage = onOpenImage,
        autoLoadImages = autoLoadImages,
    )
}
