package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardTitle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostLinkBanner
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.handleUrl
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen

@Composable
internal fun PostReportCard(
    report: PostReportModel,
    postLayout: PostLayout = PostLayout.Card,
    modifier: Modifier = Modifier,
    autoLoadImages: Boolean = true,
    onOpen: (() -> Unit)? = null,
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val detailOpener = remember { getDetailOpener() }

    InnerReportCard(
        modifier = modifier,
        reason = report.reason.orEmpty(),
        postLayout = postLayout,
        creator = report.creator,
        date = report.publishDate,
        autoLoadImages = autoLoadImages,
        options = options,
        onOptionSelected = onOptionSelected,
        onOpen = onOpen,
        originalContent = {
            Column {
                report.originalTitle?.also { title ->
                    PostCardTitle(
                        modifier = Modifier.padding(
                            vertical = Spacing.xs,
                            horizontal = Spacing.xs,
                        ),
                        text = title,
                        autoLoadImages = autoLoadImages,
                        onOpenUser = rememberCallbackArgs { user, instance ->
                            detailOpener.openUserDetail(user, instance)
                        },
                        onOpenPost = rememberCallbackArgs { post, instance ->
                            detailOpener.openPostDetail(post, instance)
                        },
                        onOpenWeb = rememberCallbackArgs { url ->
                            navigationCoordinator.pushScreen(
                                WebViewScreen(url)
                            )
                        },
                    )
                }
                report.imageUrl.takeIf { it.isNotEmpty() }?.also { imageUrl ->
                    PostCardImage(
                        modifier = Modifier
                            .padding(vertical = Spacing.xxs)
                            .clip(RoundedCornerShape(CornerSize.xl)),
                        imageUrl = imageUrl,
                        autoLoadImages = autoLoadImages,
                    )
                }
                report.originalText?.also { text ->
                    PostCardBody(
                        modifier = Modifier.padding(
                            vertical = Spacing.xs,
                            horizontal = Spacing.xs,
                        ),
                        text = text,
                        autoLoadImages = autoLoadImages,
                        onOpenUser = rememberCallbackArgs { user, instance ->
                            detailOpener.openUserDetail(user, instance)
                        },
                        onOpenPost = rememberCallbackArgs { post, instance ->
                            detailOpener.openPostDetail(post, instance)
                        },
                        onOpenWeb = rememberCallbackArgs { url ->
                            navigationCoordinator.pushScreen(
                                WebViewScreen(url)
                            )
                        },
                    )
                }
                report.originalUrl?.also { url ->
                    val settingsRepository = remember { getSettingsRepository() }
                    val uriHandler = LocalUriHandler.current
                    PostLinkBanner(
                        modifier = Modifier
                            .padding(vertical = Spacing.xs)
                            .onClick(
                                onClick = rememberCallback {
                                    navigationCoordinator.handleUrl(
                                        url = url,
                                        openExternal = settingsRepository.currentSettings.value.openUrlsInExternalBrowser,
                                        uriHandler = uriHandler
                                    )
                                },
                            ),
                        url = url,
                    )
                }
            }
        }
    )
}