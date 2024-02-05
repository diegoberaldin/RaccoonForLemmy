package com.github.diegoberaldin.raccoonforlemmy.unit.userinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.DetailInfoItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.getPrettyNumber
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableName
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.components.ModeratedCommunityCell
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class UserInfoScreen(
    private val userId: Int,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<UserInfoMviModel> { parametersOf(userId) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scope = rememberCoroutineScope()
        val detailOpener = remember { getDetailOpener() }
        val themeRepository = remember { getThemeRepository() }
        val family by themeRepository.contentFontFamily.collectAsState()
        val typography = family.toTypography()

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = Spacing.s,
                    start = Spacing.s,
                    end = Spacing.s,
                    bottom = Spacing.m,
                )
                .fillMaxHeight(0.9f)
                .fillMaxWidth(),
        ) {
            BottomSheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier.padding(top = Spacing.s),
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = uiState.user.readableName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = Spacing.xs, start = Spacing.m, end = Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Cake,
                                title = uiState.user.accountAge.prettifyDate(),
                            )
                            uiState.user.score?.also { score ->
                                DetailInfoItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Default.Padding,
                                    title = stringResource(MR.strings.community_info_posts),
                                    value = score.postScore.getPrettyNumber(
                                        thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                        millionLabel = stringResource(MR.strings.profile_million_short),
                                    ),
                                )
                                DetailInfoItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Default.Reply,
                                    title = stringResource(MR.strings.community_info_comments),
                                    value = score.commentScore.getPrettyNumber(
                                        thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                        millionLabel = stringResource(MR.strings.profile_million_short),
                                    ),
                                )
                            }
                            if (uiState.user.admin) {
                                DetailInfoItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = Icons.Default.Shield,
                                    title = stringResource(MR.strings.user_info_admin),
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
                                verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                            ) {
                                Text(
                                    text = stringResource(MR.strings.settings_web_bio),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                                )
                                CustomizedContent {
                                    PostCardBody(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = biography,
                                        onOpenImage = { url ->
                                            navigationCoordinator.hideBottomSheet()
                                            scope.launch {
                                                delay(100)
                                                navigationCoordinator.pushScreen(
                                                    ZoomableImageScreen(
                                                        url
                                                    )
                                                )
                                            }
                                        },
                                        onOpenCommunity = { community, instance ->
                                            navigationCoordinator.hideBottomSheet()
                                            scope.launch {
                                                delay(100)
                                                detailOpener.openCommunityDetail(
                                                    community,
                                                    instance,
                                                )

                                            }
                                        },
                                        onOpenPost = { post, instance ->
                                            navigationCoordinator.hideBottomSheet()
                                            scope.launch {
                                                delay(100)
                                                detailOpener.openPostDetail(post, instance)

                                            }
                                        },
                                        onOpenUser = { user, instance ->
                                            navigationCoordinator.hideBottomSheet()
                                            scope.launch {
                                                delay(100)
                                                detailOpener.openUserDetail(user, instance)
                                            }
                                        },
                                        onOpenWeb = { url ->
                                            navigationCoordinator.hideBottomSheet()
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
                                verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                            ) {
                                Text(
                                    text = stringResource(MR.strings.settings_web_matrix),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                                )
                                CustomizedContent {
                                    SelectionContainer {
                                        Text(
                                            text = matrixUserId,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Monospace
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = Spacing.s,
                                        bottom = Spacing.xs,
                                    ),
                                text = stringResource(MR.strings.user_info_moderates),
                            )
                            LazyRow(
                                modifier = Modifier.padding(top = Spacing.xxs),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                            ) {
                                items(
                                    count = uiState.moderatedCommunities.size
                                ) { idx ->
                                    val community = uiState.moderatedCommunities[idx]
                                    ModeratedCommunityCell(
                                        autoLoadImages = uiState.autoLoadImages,
                                        community = community,
                                        onOpenCommunity = rememberCallbackArgs { _ ->
                                            navigationCoordinator.hideBottomSheet()
                                            scope.launch {
                                                delay(100)
                                                detailOpener.openCommunityDetail(community, "")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
