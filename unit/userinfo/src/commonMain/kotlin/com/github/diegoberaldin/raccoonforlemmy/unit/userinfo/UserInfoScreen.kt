package com.github.diegoberaldin.raccoonforlemmy.unit.userinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.DetailInfoItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.getPrettyNumber
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableName
import com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.components.ModeratedCommunityCell
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class UserInfoScreen(
    private val userId: Long,
    private val username: String,
    private val otherInstance: String,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model =
            getScreenModel<UserInfoMviModel>(
                tag = userId.toString(),
                parameters = { parametersOf(userId, username, otherInstance) },
            )
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scope = rememberCoroutineScope()
        val detailOpener = remember { getDetailOpener() }
        val themeRepository = remember { getThemeRepository() }
        val family by themeRepository.contentFontFamily.collectAsState()
        val typography = family.toTypography()

        Scaffold(
            contentColor = MaterialTheme.colorScheme.onBackground,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            topBar = {
                val title = uiState.user.readableName(uiState.preferNicknames)
                TopAppBar(
                    modifier = Modifier.padding(top = Spacing.s),
                    colors =
                        TopAppBarDefaults.topAppBarColors().copy(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        ),
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    actions = {
                        Icon(
                            modifier =
                                Modifier.padding(end = Spacing.s).onClick(
                                    onClick = {
                                        navigationCoordinator.closeSideMenu()
                                    },
                                ),
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                        )
                        .padding(
                            top = Spacing.s,
                            start = Spacing.m,
                            end = Spacing.m,
                        ),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        SelectionContainer {
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.AlternateEmail,
                                title = uiState.user.readableHandle,
                            )
                        }
                        DetailInfoItem(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Cake,
                            title = uiState.user.accountAge.prettifyDate(),
                        )
                        uiState.user.score?.also { score ->
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.AutoMirrored.Default.Article,
                                title = LocalXmlStrings.current.communityInfoPosts,
                                value =
                                    score.postScore.getPrettyNumber(
                                        thousandLabel = LocalXmlStrings.current.profileThousandShort,
                                        millionLabel = LocalXmlStrings.current.profileMillionShort,
                                    ),
                            )
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.AutoMirrored.Default.Reply,
                                title = LocalXmlStrings.current.communityInfoComments,
                                value =
                                    score.commentScore.getPrettyNumber(
                                        thousandLabel = LocalXmlStrings.current.profileThousandShort,
                                        millionLabel = LocalXmlStrings.current.profileMillionShort,
                                    ),
                            )
                        }
                        if (uiState.user.admin) {
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Shield,
                                title = LocalXmlStrings.current.userInfoAdmin,
                            )
                        }
                    }
                }
                uiState.user.displayName.takeIf { it.isNotEmpty() }?.also { name ->
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = name,
                            textAlign = TextAlign.Center,
                            style = typography.titleMedium,
                        )
                    }
                }
                uiState.user.bio?.takeIf { it.isNotEmpty() }?.also { biography ->
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        ) {
                            Text(
                                text = LocalXmlStrings.current.settingsWebBio,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha),
                            )
                            CustomizedContent(ContentFontClass.Body) {
                                PostCardBody(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = biography,
                                    onOpenImage = { url ->
                                        navigationCoordinator.closeSideMenu()
                                        scope.launch {
                                            delay(100)
                                            navigationCoordinator.pushScreen(
                                                ZoomableImageScreen(
                                                    url = url,
                                                    source = uiState.user.readableHandle,
                                                ),
                                            )
                                        }
                                    },
                                    onOpenCommunity = { community, instance ->
                                        navigationCoordinator.closeSideMenu()
                                        scope.launch {
                                            delay(100)
                                            detailOpener.openCommunityDetail(
                                                community,
                                                instance,
                                            )
                                        }
                                    },
                                    onOpenPost = { post, instance ->
                                        navigationCoordinator.closeSideMenu()
                                        scope.launch {
                                            delay(100)
                                            detailOpener.openPostDetail(post, instance)
                                        }
                                    },
                                    onOpenUser = { user, instance ->
                                        navigationCoordinator.closeSideMenu()
                                        scope.launch {
                                            delay(100)
                                            detailOpener.openUserDetail(user, instance)
                                        }
                                    },
                                    onOpenWeb = { url ->
                                        navigationCoordinator.closeSideMenu()
                                        scope.launch {
                                            delay(100)
                                            navigationCoordinator.pushScreen(WebViewScreen(url))
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                uiState.user.matrixUserId?.also { matrixUserId ->
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        ) {
                            Text(
                                text = LocalXmlStrings.current.settingsWebMatrix,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha),
                            )
                            CustomizedContent(ContentFontClass.AncillaryText) {
                                SelectionContainer {
                                    Text(
                                        text = matrixUserId,
                                        style =
                                            MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                            ),
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.moderatedCommunities.isNotEmpty()) {
                    item {
                        Text(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = Spacing.s,
                                        bottom = Spacing.xs,
                                    ),
                            text = LocalXmlStrings.current.userInfoModerates,
                        )
                        LazyRow(
                            modifier = Modifier.padding(top = Spacing.xxs),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                        ) {
                            items(
                                count = uiState.moderatedCommunities.size,
                            ) { idx ->
                                val community = uiState.moderatedCommunities[idx]
                                ModeratedCommunityCell(
                                    autoLoadImages = uiState.autoLoadImages,
                                    community = community,
                                    onOpenCommunity =
                                        rememberCallbackArgs { _ ->
                                            navigationCoordinator.closeSideMenu()
                                            scope.launch {
                                                delay(100)
                                                detailOpener.openCommunityDetail(community)
                                            }
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
