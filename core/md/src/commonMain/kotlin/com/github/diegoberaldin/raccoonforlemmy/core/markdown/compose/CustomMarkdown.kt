package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownColors
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.MarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownColor
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownTypography
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor

@Composable
expect fun CustomMarkdown(
    content: String,
    colors: MarkdownColors = markdownColor(
        text = MaterialTheme.colorScheme.onBackground,
        backgroundCode = MaterialTheme.colorScheme.background,
    ),
    typography: MarkdownTypography = markdownTypography(
        h1 = MaterialTheme.typography.titleLarge,
        h2 = MaterialTheme.typography.titleLarge,
        h3 = MaterialTheme.typography.titleMedium,
        h4 = MaterialTheme.typography.titleMedium,
        h5 = MaterialTheme.typography.titleSmall,
        h6 = MaterialTheme.typography.titleSmall,
        text = MaterialTheme.typography.bodyMedium,
        paragraph = MaterialTheme.typography.bodyMedium,
    ),
    padding: MarkdownPadding = markdownPadding(),
    modifier: Modifier = Modifier.fillMaxSize(),
    flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor(),
    onOpenUrl: ((String) -> Unit)? = null,
    inlineImages: Boolean = true,
    autoLoadImages: Boolean = true,
    onOpenImage: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
)