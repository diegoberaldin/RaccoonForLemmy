package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.MarkdownColors
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.MarkdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.MarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.markdownColor
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.markdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.markdownTypography

@Composable
expect fun CustomMarkdown(
    content: String,
    modifier: Modifier = Modifier.fillMaxSize(),
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
    maxLines: Int? = null,
    onOpenUrl: ((String) -> Unit)? = null,
    inlineImages: Boolean = true,
    autoLoadImages: Boolean = true,
    onOpenImage: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
)