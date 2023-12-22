package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalBulletListHandler
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownColors
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalMarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.LocalOrderedListHandler
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.utils.buildMarkdownAnnotatedString
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.utils.filterNonListTypes
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownElementTypes.ORDERED_LIST
import org.intellij.markdown.MarkdownElementTypes.UNORDERED_LIST
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode

@Composable
private fun MarkdownListItems(
    content: String,
    node: ASTNode,
    style: TextStyle = LocalMarkdownTypography.current.list,
    level: Int = 0,
    autoLoadImages: Boolean = true,
    item: @Composable (child: ASTNode) -> Unit,
) {
    val listDp = LocalMarkdownPadding.current.list
    val indentListDp = LocalMarkdownPadding.current.indentList
    Column(
        modifier = Modifier.padding(
            start = (indentListDp) * level,
            top = listDp,
            bottom = listDp,
        ),
    ) {
        node.children.forEach { child ->
            when (child.type) {
                MarkdownElementTypes.LIST_ITEM -> {
                    item(child)
                    when (child.children.last().type) {
                        ORDERED_LIST -> MarkdownOrderedList(
                            content = content,
                            node = child,
                            style = style,
                            level = level + 1,
                            autoLoadImages = autoLoadImages,
                        )

                        UNORDERED_LIST -> MarkdownBulletList(
                            content = content,
                            node = child,
                            style = style,
                            level = level + 1,
                            autoLoadImages = autoLoadImages,
                        )
                    }
                }

                ORDERED_LIST -> MarkdownOrderedList(
                    content = content,
                    node = child,
                    style = style,
                    level = level + 1,
                    autoLoadImages = autoLoadImages,
                )

                UNORDERED_LIST -> MarkdownBulletList(
                    content = content,
                    node = child,
                    style = style,
                    level = level + 1,
                    autoLoadImages = autoLoadImages,
                )
            }
        }
    }
}

@Composable
internal fun MarkdownOrderedList(
    content: String,
    node: ASTNode,
    style: TextStyle = LocalMarkdownTypography.current.ordered,
    level: Int = 0,
    autoLoadImages: Boolean = true,
    onOpenUrl: ((String) -> Unit)? = null,
) {
    val orderedListHandler = LocalOrderedListHandler.current
    MarkdownListItems(
        content = content,
        node = node,
        style = style,
        level = level,
        autoLoadImages = autoLoadImages,
    ) { child ->
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = orderedListHandler.transform(
                    child.findChildOfType(MarkdownTokenTypes.LIST_NUMBER)?.getTextInNode(content),
                ),
                style = style,
                color = LocalMarkdownColors.current.text,
            )
            val text = buildAnnotatedString {
                pushStyle(style.toSpanStyle())
                buildMarkdownAnnotatedString(
                    content = content,
                    children = child.children.filterNonListTypes(),
                    linkColor = LocalMarkdownColors.current.linkColor,
                )
                pop()
            }
            MarkdownText(
                text,
                Modifier.padding(bottom = 4.dp),
                style = style,
                onOpenUrl = onOpenUrl,
                autoLoadImages = autoLoadImages,
            )
        }
    }
}

@Composable
internal fun MarkdownBulletList(
    content: String,
    node: ASTNode,
    style: TextStyle = LocalMarkdownTypography.current.bullet,
    level: Int = 0,
    autoLoadImages: Boolean = true,
    onOpenUrl: ((String) -> Unit)? = null,
) {
    val bulletHandler = LocalBulletListHandler.current
    MarkdownListItems(
        content = content,
        node = node,
        style = style,
        level = level,
        autoLoadImages = autoLoadImages,
    ) { child ->
        Row(Modifier.fillMaxWidth()) {
            Text(
                bulletHandler.transform(
                    child.findChildOfType(MarkdownTokenTypes.LIST_BULLET)?.getTextInNode(content),
                ),
                style = style,
                color = LocalMarkdownColors.current.text,
            )
            val text = buildAnnotatedString {
                pushStyle(style.toSpanStyle())
                buildMarkdownAnnotatedString(
                    content = content,
                    children = child.children.filterNonListTypes(),
                    linkColor = LocalMarkdownColors.current.linkColor
                )
                pop()
            }
            MarkdownText(
                text,
                Modifier.padding(bottom = 4.dp),
                style = style,
                onOpenUrl = onOpenUrl,
                autoLoadImages = autoLoadImages,
            )
        }
    }
}
