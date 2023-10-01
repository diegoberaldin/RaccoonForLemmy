package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.Markdown
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownColor
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository

@Composable
fun PostCardBody(
    modifier: Modifier = Modifier,
    text: String,
) {
    val uriHandler = LocalUriHandler.current
    val navigator = remember { getNavigationCoordinator().getRootNavigator() }
    val settingsRepository = remember { getSettingsRepository() }
    val settings by settingsRepository.currentSettings.collectAsState()
    val openExternal = settings.openUrlsInExternalBrowser

    if (text.isNotEmpty()) {
        Markdown(
            modifier = modifier,
            content = text,
            typography = markdownTypography(
                h1 = MaterialTheme.typography.titleLarge,
                h2 = MaterialTheme.typography.titleLarge,
                h3 = MaterialTheme.typography.titleMedium,
                h4 = MaterialTheme.typography.titleMedium,
                h5 = MaterialTheme.typography.titleSmall,
                h6 = MaterialTheme.typography.titleSmall,
                text = MaterialTheme.typography.bodyMedium,
                paragraph = MaterialTheme.typography.bodyMedium,
            ),
            colors = markdownColor(
                text = MaterialTheme.colorScheme.onBackground,
                backgroundCode = MaterialTheme.colorScheme.background,
            ),
            inlineImages = false,
            onOpenUrl = { url ->
                handleUrl(
                    url = url,
                    openExternal = openExternal,
                    uriHandler = uriHandler,
                    navigator = navigator
                )
            }
        )
    }
}
