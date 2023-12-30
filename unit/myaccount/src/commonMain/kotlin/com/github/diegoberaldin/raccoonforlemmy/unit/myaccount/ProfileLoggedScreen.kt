package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.ProfileLoggedSection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.UserHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ShareBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object ProfileLoggedScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ProfileLoggedMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val user = uiState.user
        val notificationCenter = remember { getNotificationCenter() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val lazyListState = rememberLazyListState()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val detailOpener = remember { getDetailOpener() }

        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.onDoubleTabSelection.onEach { section ->
                if (section == TabNavigationSection.Profile) {
                    lazyListState.scrollToItem(0)
                }
            }.launchIn(this)
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.subscribe(NotificationCenterEvent.PostCreated::class).onEach {
                model.reduce(ProfileLoggedMviModel.Intent.Refresh)
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.CommentCreated::class).onEach {
                model.reduce(ProfileLoggedMviModel.Intent.Refresh)
            }.launchIn(this)
        }

        if (user != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxxs),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh = rememberCallback(model) {
                        model.reduce(ProfileLoggedMviModel.Intent.Refresh)
                    },
                )
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
                                    onOpenImage = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(ZoomableImageScreen(url))
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
                                    onSectionSelected = rememberCallbackArgs(model) { idx ->
                                        val section = when (idx) {
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
                            items(
                                items = uiState.posts,
                                key = { it.id.toString() + it.updateDate },
                            ) { post ->
                                PostCard(
                                    post = post,
                                    postLayout = uiState.postLayout,
                                    fullHeightImage = uiState.fullHeightImages,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    hideAuthor = true,
                                    blurNsfw = false,
                                    onClick = rememberCallback {
                                        detailOpener.openPostDetail(post)
                                    },
                                    onOpenCommunity = rememberCallbackArgs { community, instance ->
                                        detailOpener.openCommunityDetail(community, instance)
                                    },
                                    onOpenCreator = rememberCallbackArgs { user, instance ->
                                        detailOpener.openUserDetail(user, instance)
                                    },
                                    onOpenPost = rememberCallbackArgs { p, instance ->
                                        detailOpener.openPostDetail(p, instance)
                                    },
                                    onOpenWeb = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(
                                            WebViewScreen(url)
                                        )
                                    },
                                    onOpenImage = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(
                                            ZoomableImageScreen(url),
                                        )
                                    },
                                    onUpVote = rememberCallback(model) {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.UpVotePost(
                                                id = post.id,
                                            )
                                        )
                                    },
                                    onDownVote = rememberCallback(model) {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.DownVotePost(
                                                id = post.id,
                                            )
                                        )
                                    },
                                    onSave = rememberCallback(model) {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.SavePost(
                                                id = post.id,
                                            )
                                        )
                                    },
                                    options = buildList {
                                        add(
                                            Option(
                                                OptionId.Share,
                                                stringResource(MR.strings.post_action_share)
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.SeeRaw,
                                                stringResource(MR.strings.post_action_see_raw)
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Edit,
                                                stringResource(MR.strings.post_action_edit)
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Delete,
                                                stringResource(MR.strings.comment_action_delete)
                                            )
                                        )
                                    },
                                    onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                        when (optionId) {
                                            OptionId.Delete -> model.reduce(
                                                ProfileLoggedMviModel.Intent.DeletePost(post.id)
                                            )

                                            OptionId.Edit -> {
                                                detailOpener.openCreatePost(
                                                    editedPost = post,
                                                )
                                            }

                                            OptionId.SeeRaw -> {
                                                rawContent = post
                                            }

                                            OptionId.Share -> {
                                                val urls = listOfNotNull(
                                                    post.originalUrl,
                                                    "https://${uiState.instance}/post/${post.id}"
                                                ).distinct()
                                                if (urls.size == 1) {
                                                    model.reduce(
                                                        ProfileLoggedMviModel.Intent.Share(
                                                            urls.first()
                                                        )
                                                    )
                                                } else {
                                                    val screen = ShareBottomSheet(urls = urls)
                                                    navigationCoordinator.showBottomSheet(screen)
                                                }
                                            }

                                            else -> Unit
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
                            items(
                                items = uiState.comments,
                                key = { it.id.toString() + it.updateDate },
                            ) { comment ->
                                CommentCard(modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                    comment = comment,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    hideCommunity = false,
                                    hideAuthor = true,
                                    hideIndent = true,
                                    onImageClick = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(ZoomableImageScreen(url))
                                    },
                                    onClick = rememberCallback {
                                        detailOpener.openPostDetail(
                                            post = PostModel(id = comment.postId),
                                            highlightCommentId = comment.id,
                                        )
                                    },
                                    onUpVote = rememberCallback(model) {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.UpVoteComment(
                                                id = comment.id,
                                            )
                                        )
                                    },
                                    onDownVote = rememberCallback(model) {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.DownVoteComment(
                                                id = comment.id,
                                            )
                                        )
                                    },
                                    onSave = rememberCallback(model) {
                                        model.reduce(
                                            ProfileLoggedMviModel.Intent.SaveComment(
                                                id = comment.id,
                                            )
                                        )
                                    },
                                    options = buildList {
                                        add(
                                            Option(
                                                OptionId.SeeRaw,
                                                stringResource(MR.strings.post_action_see_raw)
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Edit,
                                                stringResource(MR.strings.post_action_edit)
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Delete,
                                                stringResource(MR.strings.comment_action_delete)
                                            )
                                        )
                                    },
                                    onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                        when (optionId) {
                                            OptionId.Delete -> {
                                                model.reduce(
                                                    ProfileLoggedMviModel.Intent.DeleteComment(
                                                        comment.id
                                                    )
                                                )
                                            }

                                            OptionId.Edit -> {
                                                detailOpener.openReply(
                                                    editedComment = comment,
                                                )
                                            }

                                            OptionId.SeeRaw -> {
                                                rawContent = comment
                                            }

                                            else -> Unit
                                        }
                                    })
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

        if (rawContent != null) {
            when (val content = rawContent) {
                is PostModel -> {
                    RawContentDialog(title = content.title,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        url = content.url,
                        text = content.text,
                        onDismiss = rememberCallback {
                            rawContent = null
                        },
                        onQuote = rememberCallbackArgs { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                detailOpener.openReply(
                                    originalPost = content,
                                    initialText = buildString {
                                        append("> ")
                                        append(quotation)
                                        append("\n\n")
                                    },
                                )
                            }
                        })
                }

                is CommentModel -> {
                    RawContentDialog(text = content.text,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        onDismiss = {
                            rawContent = null
                        },
                        onQuote = rememberCallbackArgs { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                detailOpener.openReply(
                                    originalComment = content,
                                    initialText = buildString {
                                        append("> ")
                                        append(quotation)
                                        append("\n\n")
                                    },
                                )
                            }
                        })
                }
            }
        }
    }
}
