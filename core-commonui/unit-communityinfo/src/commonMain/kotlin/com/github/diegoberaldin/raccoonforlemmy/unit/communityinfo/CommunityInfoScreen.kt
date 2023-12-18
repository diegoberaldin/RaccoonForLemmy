package com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.getPrettyNumber
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di.getCommunityInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CommunityInfoScreen(
    private val community: CommunityModel,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getCommunityInfoViewModel(community) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scope = rememberCoroutineScope()
        val detailOpener = remember { getDetailOpener() }

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
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            BottomSheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = buildString {
                                        append(uiState.community.name)
                                        if (uiState.community.host.isNotEmpty()) {
                                            append("@${uiState.community.host}")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        })
                },
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(top = Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Cake,
                                title = community.creationDate?.prettifyDate().orEmpty(),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Padding,
                                title = stringResource(MR.strings.community_info_posts),
                                value = uiState.community.posts.getPrettyNumber(
                                    thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                    millionLabel = stringResource(MR.strings.profile_million_short),
                                ),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Reply,
                                title = stringResource(MR.strings.community_info_comments),
                                value = uiState.community.comments.getPrettyNumber(
                                    thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                    millionLabel = stringResource(MR.strings.profile_million_short),
                                ),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Group,
                                title = stringResource(MR.strings.community_info_subscribers),
                                value = uiState.community.subscribers.getPrettyNumber(
                                    thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                    millionLabel = stringResource(MR.strings.profile_million_short),
                                ),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewMonth,
                                title = stringResource(MR.strings.community_info_monthly_active_users),
                                value = uiState.community.monthlyActiveUsers.getPrettyNumber(
                                    thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                    millionLabel = stringResource(MR.strings.profile_million_short),
                                ),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewWeek,
                                title = stringResource(MR.strings.community_info_weekly_active_users),
                                value = uiState.community.weeklyActiveUsers.getPrettyNumber(
                                    thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                    millionLabel = stringResource(MR.strings.profile_million_short),
                                ),
                            )
                            CommunityInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewDay,
                                title = stringResource(MR.strings.community_info_daily_active_users),
                                value = uiState.community.dailyActiveUsers.getPrettyNumber(
                                    thousandLabel = stringResource(MR.strings.profile_thousand_short),
                                    millionLabel = stringResource(MR.strings.profile_million_short),
                                ),
                            )
                        }
                    }
                    if (uiState.moderators.isNotEmpty()) {
                        item {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = Spacing.s,
                                        bottom = Spacing.xs,
                                    ),
                                text = stringResource(MR.strings.community_info_moderators),
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                            ) {
                                items(
                                    count = uiState.moderators.size
                                ) { idx ->
                                    val user = uiState.moderators[idx]
                                    ModeratorCell(
                                        autoLoadImages = uiState.autoLoadImages,
                                        user = user,
                                        onOpenUser = rememberCallbackArgs { _ ->
                                            navigationCoordinator.hideBottomSheet()
                                            scope.launch {
                                                delay(100)
                                                detailOpener.openUserDetail(user, "")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        CustomizedContent {
                            PostCardBody(
                                modifier = Modifier.fillMaxWidth().padding(top = Spacing.m),
                                text = uiState.community.description,
                                onOpenImage = { url ->
                                    navigationCoordinator.hideBottomSheet()
                                    scope.launch {
                                        delay(100)
                                        navigationCoordinator.pushScreen(ZoomableImageScreen(url))
                                    }
                                },
                                onOpenCommunity = { community, instance ->
                                    navigationCoordinator.hideBottomSheet()
                                    scope.launch {
                                        delay(100)
                                        detailOpener.openCommunityDetail(community, instance)

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
        }
    }
}
