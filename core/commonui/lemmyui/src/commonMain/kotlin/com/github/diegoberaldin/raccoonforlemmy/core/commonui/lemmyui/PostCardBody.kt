package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.CustomMarkdownWrapper
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.mikepenz.markdown.model.markdownColor
import com.mikepenz.markdown.model.markdownTypography

@Composable
fun PostCardBody(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int? = null,
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
        CustomMarkdownWrapper(
            modifier = modifier,
            content = text,
            maxLines = maxLines,
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
                quote = typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                bullet = typography.bodyMedium,
                list = typography.bodyMedium,
                code = typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            ),
            colors = markdownColor(
                text = MaterialTheme.colorScheme.onBackground,
                linkText = MaterialTheme.colorScheme.primary,
                codeText = MaterialTheme.colorScheme.onBackground,
                codeBackground = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
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
