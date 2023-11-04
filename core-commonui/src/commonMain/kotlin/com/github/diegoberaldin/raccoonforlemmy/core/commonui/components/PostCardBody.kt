package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.CustomMarkdown
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository

@Composable
fun PostCardBody(
    modifier: Modifier = Modifier,
    text: String,
    autoLoadImages: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val settingsRepository = remember { getSettingsRepository() }

    if (text.isNotEmpty()) {
        CustomMarkdown(
            modifier = modifier,
            content = text,
            inlineImages = false,
            autoLoadImages = autoLoadImages,
            onOpenUrl = { url ->
                handleUrl(
                    url = url,
                    openExternal = settingsRepository.currentSettings.value.openUrlsInExternalBrowser,
                    uriHandler = uriHandler,
                    navigator = navigationCoordinator.getRootNavigator()
                )
            },
            onOpenImage = { url ->
                navigationCoordinator.getRootNavigator()?.push(ZoomableImageScreen(url))
            },
            onClick = onClick,
        )
    }
}
