package com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.getProfileLoggedViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.ui.ProfileTab
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal object ProfileLoggedScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxxs),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val model = rememberScreenModel { getProfileLoggedViewModel() }
            model.bindToLifecycle(key)
            val uiState by model.uiState.collectAsState()
            val user = uiState.user
            val notificationCenter = remember { getNotificationCenter() }
            val navigationCoordinator = remember { getNavigationCoordinator() }
            val navigator = remember { navigationCoordinator.getRootNavigator() }
            val bottomSheetNavigator = LocalBottomSheetNavigator.current
            val lazyListState = rememberLazyListState()
            LaunchedEffect(navigator) {
                navigationCoordinator.onDoubleTabSelection.onEach { tab ->
                    if (tab == ProfileTab) {
                        lazyListState.scrollToItem(0)
                    }
                }.launchIn(this)
            }
            DisposableEffect(key) {
                onDispose {
                    notificationCenter.removeObserver(key)
                }
            }

            if (user != null) {
                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(ProfileLoggedMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier.pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                    ) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                            ) {
                                UserHeader(
                                    user = user,
                                    autoLoadImages = uiState.autoLoadImages,
                                    onOpenImage = { url ->
                                        navigator?.push(ZoomableImageScreen(url))
                                    },
                                )
                                SectionSelector(
                                    titles = listOf(
                                        stringResource(MR.strings.profile_section_posts),
                                        stringResource(MR.strings.profile_section_comments),
                                    ),
                                    currentSection = when (uiState.section) {
                                        ProfileLoggedSection.Comments -> 1
                                        else -> 0
                                    },
                                    onSectionSelected = {
                                        val section = when (it) {
                                            1 -> ProfileLoggedSection.Comments
                                            else -> ProfileLoggedSection.Posts
                                        }
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.ChangeSection(
                                                section
                                            )
                                        )
                                    },
                                )
                                Spacer(modifier = Modifier.height(Spacing.m))
                            }
                        }
                        if (uiState.section == ProfileLoggedSection.Posts) {
                            if (uiState.posts.isEmpty() && uiState.loading && !uiState.initial) {
                                items(5) {
                                    PostCardPlaceholder(
                                        postLayout = uiState.postLayout,
                                    )
                                    if (uiState.postLayout != PostLayout.Card) {
                                        Divider(modifier = Modifier.padding(vertical = Spacing.s))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.s))
                                    }
                                }
                            }
                            itemsIndexed(uiState.posts) { idx, post ->
                                PostCard(
                                    post = post,
                                    postLayout = uiState.postLayout,
                                    fullHeightImage = uiState.fullHeightImages,
                                    separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                    autoLoadImages = uiState.autoLoadImages,
                                    hideAuthor = true,
                                    blurNsfw = false,
                                    onClick = {
                                        navigator?.push(
                                            PostDetailScreen(post),
                                        )
                                    },
                                    onOpenCommunity = { community ->
                                        navigator?.push(
                                            CommunityDetailScreen(community),
                                        )
                                    },
                                    onImageClick = { url ->
                                        navigator?.push(
                                            ZoomableImageScreen(url),
                                        )
                                    },
                                    onUpVote = {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.UpVotePost(
                                                idx,
                                                true
                                            )
                                        )
                                    },
                                    onDownVote = {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.DownVotePost(
                                                idx,
                                                true
                                            )
                                        )
                                    },
                                    onSave = {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.SavePost(
                                                idx,
                                                true
                                            )
                                        )
                                    },
                                    options = buildList {
                                        add(stringResource(MR.strings.post_action_share))
                                        add(stringResource(MR.strings.post_action_edit))
                                        add(stringResource(MR.strings.comment_action_delete))
                                    },
                                    onOptionSelected = { optionIdx ->
                                        when (optionIdx) {
                                            1 -> {
                                                notificationCenter.addObserver(
                                                    {
                                                        model.reduce(ProfileLoggedMviModel.Intent.Refresh)
                                                    },
                                                    key,
                                                    NotificationCenterContractKeys.PostCreated
                                                )
                                                bottomSheetNavigator.show(
                                                    CreatePostScreen(
                                                        editedPost = post,
                                                    )
                                                )
                                            }

                                            2 -> model.reduce(
                                                ProfileLoggedMviModel.Intent.DeletePost(post.id)
                                            )

                                            else -> model.reduce(
                                                ProfileLoggedMviModel.Intent.SharePost(idx)
                                            )
                                        }
                                    },
                                )
                                if (uiState.postLayout != PostLayout.Card) {
                                    Divider(modifier = Modifier.padding(vertical = Spacing.s))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.s))
                                }
                            }

                            if (uiState.posts.isEmpty() && !uiState.loading && !uiState.initial) {
                                item {
                                    Text(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(top = Spacing.xs),
                                        textAlign = TextAlign.Center,
                                        text = stringResource(MR.strings.message_empty_list),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                        } else {
                            if (uiState.comments.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    CommentCardPlaceholder(hideAuthor = true)
                                    Divider(
                                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                                        thickness = 0.25.dp
                                    )
                                }
                            }
                            itemsIndexed(uiState.comments) { idx, comment ->
                                CommentCard(
                                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                    comment = comment,
                                    separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                    autoLoadImages = uiState.autoLoadImages,
                                    hideCommunity = false,
                                    hideAuthor = true,
                                    hideIndent = true,
                                    onClick = {
                                        navigator?.push(
                                            PostDetailScreen(
                                                post = PostModel(id = comment.postId),
                                                highlightCommentId = comment.id,
                                            ),
                                        )
                                    },
                                    onUpVote = {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.UpVoteComment(
                                                idx,
                                                true
                                            )
                                        )
                                    },
                                    onDownVote = {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.DownVoteComment(
                                                idx,
                                                true
                                            )
                                        )
                                    },
                                    onSave = {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.SaveComment(
                                                idx,
                                                true
                                            )
                                        )
                                    },
                                    options = buildList {
                                        add(stringResource(MR.strings.post_action_edit))
                                        add(stringResource(MR.strings.comment_action_delete))
                                    },
                                    onOptionSelected = { optionIdx ->
                                        when (optionIdx) {
                                            1 -> {
                                                model.reduce(
                                                    ProfileLoggedMviModel.Intent.DeleteComment(
                                                        comment.id
                                                    )
                                                )
                                            }

                                            else -> {
                                                notificationCenter.addObserver(
                                                    {
                                                        model.reduce(ProfileLoggedMviModel.Intent.Refresh)
                                                    },
                                                    key,
                                                    NotificationCenterContractKeys.CommentCreated
                                                )
                                                bottomSheetNavigator.show(
                                                    CreateCommentScreen(
                                                        editedComment = comment,
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )
                                Divider(
                                    modifier = Modifier.padding(vertical = Spacing.xxxs),
                                    thickness = 0.25.dp
                                )
                            }

                            if (uiState.comments.isEmpty() && !uiState.loading && !uiState.initial) {
                                item {
                                    Text(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(top = Spacing.xs),
                                        textAlign = TextAlign.Center,
                                        text = stringResource(MR.strings.message_empty_list),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(Spacing.xxxl))
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(ProfileLoggedMviModel.Intent.LoadNextPage)
                            }
                            if (uiState.loading && !uiState.refreshing) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.xs),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(25.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = uiState.refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}
