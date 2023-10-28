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
fun PostCardTitle(
    text: String,
    autoLoadImages: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val navigator = remember { getNavigationCoordinator().getRootNavigator() }
    val settingsRepository = remember { getSettingsRepository() }

    CustomMarkdown(
        modifier = modifier,
        content = text,
        autoLoadImages = autoLoadImages,
        onOpenUrl = { url ->
            handleUrl(
                url = url,
                openExternal = settingsRepository.currentSettings.value.openUrlsInExternalBrowser,
                uriHandler = uriHandler,
                navigator = navigator
            )
        },
        onOpenImage = { url ->
            navigator?.push(ZoomableImageScreen(url))
        },
        onClick = onClick,
    )
}
