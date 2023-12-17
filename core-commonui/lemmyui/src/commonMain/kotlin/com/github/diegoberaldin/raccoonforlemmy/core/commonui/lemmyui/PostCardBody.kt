package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.CustomMarkdown
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.model.markdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
fun PostCardBody(
    modifier: Modifier = Modifier,
    text: String,
    autoLoadImages: Boolean = true,
    onClick: (() -> Unit)? = null,
    onOpenImage: ((String) -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onOpenCommunity: ((CommunityModel, String) -> Unit)? = null,
    onOpenUser: ((UserModel, String) -> Unit)? = null,
    onOpenPost: ((PostModel, String) -> Unit)? = null,
    onOpenWeb: ((String) -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val settingsRepository = remember { getSettingsRepository() }
    val themeRepository = remember { getThemeRepository() }
    val fontFamily by themeRepository.contentFontFamily.collectAsState()
    val typography = fontFamily.toTypography()

    if (text.isNotEmpty()) {
        CustomMarkdown(
            modifier = modifier,
            content = text,
            inlineImages = false,
            autoLoadImages = autoLoadImages,
            typography = markdownTypography(
                h1 = typography.titleLarge,
                h2 = typography.titleLarge,
                h3 = typography.titleMedium,
                h4 = typography.titleMedium,
                h5 = typography.titleSmall,
                h6 = typography.titleSmall,
                text = typography.bodyMedium,
                paragraph = typography.bodyMedium,
            ),
            onOpenUrl = rememberCallbackArgs { url ->
                navigationCoordinator.handleUrl(
                    url = url,
                    openExternal = settingsRepository.currentSettings.value.openUrlsInExternalBrowser,
                    uriHandler = uriHandler,
                    onOpenCommunity = onOpenCommunity,
                    onOpenUser = onOpenUser,
                    onOpenPost = onOpenPost,
                    onOpenWeb = onOpenWeb,
                )
            },
            onOpenImage = rememberCallbackArgs { url ->
                onOpenImage?.invoke(url)
            },
            onClick = onClick,
            onDoubleClick = onDoubleClick,
            onLongClick = onLongClick,
        )
    }
}
