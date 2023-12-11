package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.CustomMarkdown
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository

@Composable
fun PostCardTitle(
    text: String,
    autoLoadImages: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val settingsRepository = remember { getSettingsRepository() }

    CustomMarkdown(
        modifier = modifier,
        content = text,
        autoLoadImages = autoLoadImages,
        typography = markdownTypography(
            h1 = MaterialTheme.typography.titleLarge,
            h2 = MaterialTheme.typography.titleLarge,
            h3 = MaterialTheme.typography.titleMedium,
            h4 = MaterialTheme.typography.titleMedium,
            h5 = MaterialTheme.typography.titleSmall,
            h6 = MaterialTheme.typography.titleSmall,
            text = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            paragraph = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        ),
        onOpenUrl = { url ->
            navigationCoordinator.handleUrl(
                url = url,
                openExternal = settingsRepository.currentSettings.value.openUrlsInExternalBrowser,
                uriHandler = uriHandler,
            )
        },
        onOpenImage = { url ->
            navigationCoordinator.pushScreen(ZoomableImageScreen(url))
        },
        onClick = onClick,
        onDoubleClick = onDoubleClick,
        onLongClick = onLongClick,
    )
}
