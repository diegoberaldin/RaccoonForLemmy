package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.BulletHandler
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownColors
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.ReferenceLinkHandler

/**
 * The CompositionLocal to provide functionality related to transforming the bullet of an ordered list
 */
val LocalBulletListHandler = staticCompositionLocalOf {
    return@staticCompositionLocalOf BulletHandler { "• " }
}

/**
 * The CompositionLocal to provide functionality related to transforming the bullet of an ordered list
 */
val LocalOrderedListHandler = staticCompositionLocalOf {
    return@staticCompositionLocalOf BulletHandler { "$it " }
}

/**
 * Local [ReferenceLinkHandler] provider
 */
val LocalReferenceLinkHandler = staticCompositionLocalOf<ReferenceLinkHandler> {
    error("CompositionLocal ReferenceLinkHandler not present")
}

/**
 * Local [MarkdownColors] provider
 */
val LocalMarkdownColors = compositionLocalOf<MarkdownColors> {
    error("No local MarkdownColors")
}

/**
 * Local [MarkdownTypography] provider
 */
val LocalMarkdownTypography = compositionLocalOf<MarkdownTypography> {
    error("No local MarkdownTypography")
}

/**
 * Local [MarkdownPadding] provider
 */
val LocalMarkdownPadding = staticCompositionLocalOf<MarkdownPadding> {
    error("No local Padding")
}
