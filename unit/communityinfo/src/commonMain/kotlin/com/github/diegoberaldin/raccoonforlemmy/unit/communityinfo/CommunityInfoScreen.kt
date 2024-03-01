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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Padding
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
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.DetailInfoItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.getPrettyNumber
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableName
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.components.ModeratorCell
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class CommunityInfoScreen(
    private val communityId: Int,
) : Screen {

    @Composable
    override fun Content() {
        val model = getScreenModel<CommunityInfoMviModel>(
            tag = communityId.toString(),
            parameters = { parametersOf(communityId) },
        )
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scope = rememberCoroutineScope()
        val detailOpener = remember { getDetailOpener() }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = Spacing.m,
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
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = uiState.community.readableName(uiState.preferNicknames),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.AlternateEmail,
                                title = uiState.community.readableHandle,
                            )
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Cake,
                                title = uiState.community.creationDate?.prettifyDate().orEmpty(),
                            )
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Padding,
                                title = LocalXmlStrings.current.communityInfoPosts,
                                value = uiState.community.posts.getPrettyNumber(
                                    thousandLabel = LocalXmlStrings.current.profileThousandShort,
                                    millionLabel = LocalXmlStrings.current.profileMillionShort,
                                ),
                            )
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.AutoMirrored.Filled.Reply,
                                title = LocalXmlStrings.current.communityInfoComments,
                                value = uiState.community.comments.getPrettyNumber(
                                    thousandLabel = LocalXmlStrings.current.profileThousandShort,
                                    millionLabel = LocalXmlStrings.current.profileMillionShort,
                                ),
                            )
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Group,
                                title = LocalXmlStrings.current.communityInfoSubscribers,
                                value = uiState.community.subscribers.getPrettyNumber(
                                    thousandLabel = LocalXmlStrings.current.profileThousandShort,
                                    millionLabel = LocalXmlStrings.current.profileMillionShort,
                                ),
                            )
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewMonth,
                                title = LocalXmlStrings.current.communityInfoMonthlyActiveUsers,
                                value = uiState.community.monthlyActiveUsers.getPrettyNumber(
                                    thousandLabel = LocalXmlStrings.current.profileThousandShort,
                                    millionLabel = LocalXmlStrings.current.profileMillionShort,
                                ),
                            )
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewWeek,
                                title = LocalXmlStrings.current.communityInfoWeeklyActiveUsers,
                                value = uiState.community.weeklyActiveUsers.getPrettyNumber(
                                    thousandLabel = LocalXmlStrings.current.profileThousandShort,
                                    millionLabel = LocalXmlStrings.current.profileMillionShort,
                                ),
                            )
                            DetailInfoItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.CalendarViewDay,
                                title = LocalXmlStrings.current.communityInfoDailyActiveUsers,
                                value = uiState.community.dailyActiveUsers.getPrettyNumber(
                                    thousandLabel = LocalXmlStrings.current.profileThousandShort,
                                    millionLabel = LocalXmlStrings.current.profileMillionShort,
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
                                text = LocalXmlStrings.current.communityInfoModerators,
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
                        CustomizedContent(ContentFontClass.Body) {
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
