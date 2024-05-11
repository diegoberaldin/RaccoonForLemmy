package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
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
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
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
        val uiState by model.uiState.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val lazyListState = rememberLazyListState()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val detailOpener = remember { getDetailOpener() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        var postIdToDelete by remember { mutableStateOf<Long?>(null) }
        var commentIdToDelete by remember { mutableStateOf<Long?>(null) }

        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.onDoubleTabSelection.onEach { section ->
                runCatching {
                    if (section == TabNavigationSection.Profile) {
                        lazyListState.scrollToItem(0)
                    }
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

        if (uiState.initial) {
            ProgressHud()
            return
        }

        Column(
            modifier = Modifier.fillMaxSize(),
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
                    if (uiState.user == null) {
                        item {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = LocalXmlStrings.current.messageAuthIssue,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    } else {
                        item {
                            uiState.user?.also { user ->
                                UserHeader(
                                    user = user,
                                    autoLoadImages = uiState.autoLoadImages,
                                    onOpenImage = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(
                                            ZoomableImageScreen(
                                                url = url,
                                                source = uiState.user?.readableHandle.orEmpty(),
                                            )
                                        )
                                    },
                                )
                            }
                        }
                        item {
                            ProfileActionMenu(
                                modifier = Modifier
                                    .padding(
                                        top = Spacing.xs,
                                        bottom = Spacing.s,
                                    )
                                    .fillMaxWidth(),
                                isModerator = uiState.user?.moderator == true,
                            )
                            HorizontalDivider()
                        }
                        item {
                            SectionSelector(
                                modifier = Modifier.padding(bottom = Spacing.s),
                                titles = listOf(
                                    LocalXmlStrings.current.profileSectionPosts,
                                    LocalXmlStrings.current.profileSectionComments,
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
                                        ProfileLoggedMviModel.Intent.ChangeSection(section)
                                    )
                                },
                            )
                        }
                        if (uiState.section == ProfileLoggedSection.Posts) {
                            if (uiState.posts.isEmpty() && uiState.loading && !uiState.initial) {
                                items(5) {
                                    PostCardPlaceholder(
                                        modifier = Modifier.padding(horizontal = Spacing.xs),
                                        postLayout = uiState.postLayout,
                                    )
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
                                    }
                                }
                            }
                            items(
                                items = uiState.posts,
                                key = { it.id.toString() + (it.updateDate ?: it.publishDate) },
                            ) { post ->
                                PostCard(
                                    post = post,
                                    postLayout = uiState.postLayout,
                                    limitBodyHeight = true,
                                    fullHeightImage = uiState.fullHeightImages,
                                    fullWidthImage = uiState.fullWidthImages,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    preferNicknames = uiState.preferNicknames,
                                    showScores = uiState.showScores,
                                    showUnreadComments = uiState.showUnreadComments,
                                    hideAuthor = true,
                                    blurNsfw = false,
                                    onClick = rememberCallback(model) {
                                        model.reduce(ProfileLoggedMviModel.Intent.WillOpenDetail)
                                        detailOpener.openPostDetail(post)
                                    },
                                    onReply = rememberCallback(model) {
                                        model.reduce(ProfileLoggedMviModel.Intent.WillOpenDetail)
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
                                            ZoomableImageScreen(
                                                url = url,
                                                source = post.community?.readableHandle.orEmpty(),
                                            ),
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
                                                LocalXmlStrings.current.postActionShare,
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.CrossPost,
                                                LocalXmlStrings.current.postActionCrossPost,
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.SeeRaw,
                                                LocalXmlStrings.current.postActionSeeRaw,
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Edit,
                                                LocalXmlStrings.current.postActionEdit,
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Delete,
                                                LocalXmlStrings.current.commentActionDelete,
                                            )
                                        )
                                    },
                                    onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                        when (optionId) {
                                            OptionId.Delete -> {
                                                postIdToDelete = post.id
                                            }

                                            OptionId.Edit -> {
                                                detailOpener.openCreatePost(
                                                    editedPost = post,
                                                )
                                            }

                                            OptionId.SeeRaw -> {
                                                rawContent = post
                                            }

                                            OptionId.CrossPost -> {
                                                detailOpener.openCreatePost(
                                                    crossPost = post,
                                                    forceCommunitySelection = true,
                                                )
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
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.interItem))
                                }
                            }

                            if (uiState.posts.isEmpty() && !uiState.loading && !uiState.initial) {
                                item {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = Spacing.xs),
                                        textAlign = TextAlign.Center,
                                        text = LocalXmlStrings.current.messageEmptyList,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                        } else {
                            if (uiState.comments.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    CommentCardPlaceholder(
                                        modifier = Modifier.padding(horizontal = Spacing.xs),
                                        hideAuthor = true,
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                                        thickness = 0.25.dp,
                                    )
                                }
                            }
                            items(
                                items = uiState.comments,
                                key = { it.id.toString() + (it.updateDate ?: it.publishDate) },
                            ) { comment ->
                                CommentCard(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(horizontal = Spacing.xs),
                                    comment = comment,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    showScores = uiState.showScores,
                                    hideCommunity = false,
                                    hideAuthor = true,
                                    hideIndent = true,
                                    onImageClick = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(
                                            ZoomableImageScreen(
                                                url = url,
                                                source = comment.community?.readableHandle.orEmpty(),
                                            )
                                        )
                                    },
                                    onOpenCommunity = rememberCallbackArgs { community, instance ->
                                        detailOpener.openCommunityDetail(community, instance)
                                    },
                                    onClick = {
                                        detailOpener.openPostDetail(
                                            post = PostModel(id = comment.postId),
                                            highlightCommentId = comment.id,
                                        )
                                    },
                                    onReply = rememberCallback {
                                        detailOpener.openReply(
                                            originalPost = PostModel(id = comment.postId),
                                            originalComment = comment,
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
                                                LocalXmlStrings.current.postActionSeeRaw,
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Edit,
                                                LocalXmlStrings.current.postActionEdit,
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Delete,
                                                LocalXmlStrings.current.commentActionDelete,
                                            )
                                        )
                                    },
                                    onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                        when (optionId) {
                                            OptionId.Delete -> {
                                                commentIdToDelete = comment.id
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
                                    },
                                )
                                HorizontalDivider(
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
                                        text = LocalXmlStrings.current.messageEmptyList,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                if (settings.infiniteScrollEnabled) {
                                    model.reduce(ProfileLoggedMviModel.Intent.LoadNextPage)
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Button(
                                            onClick = rememberCallback(model) {
                                                model.reduce(ProfileLoggedMviModel.Intent.LoadNextPage)
                                            },
                                        ) {
                                            Text(
                                                text = if (uiState.section == ProfileLoggedSection.Posts) {
                                                    LocalXmlStrings.current.postListLoadMorePosts
                                                } else {
                                                    LocalXmlStrings.current.postDetailLoadMoreComments
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    }
                                }
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
                    item {
                        Spacer(modifier = Modifier.height(Spacing.xxxl))
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

        if (rawContent != null) {
            when (val content = rawContent) {
                is PostModel -> {
                    RawContentDialog(
                        title = content.title,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        url = content.url,
                        text = content.text,
                        upVotes = content.upvotes,
                        downVotes = content.downvotes,
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
                        },
                    )
                }

                is CommentModel -> {
                    RawContentDialog(
                        text = content.text,
                        upVotes = content.upvotes,
                        downVotes = content.downvotes,
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
                        },
                    )
                }
            }
        }

        postIdToDelete?.also { itemId ->
            AlertDialog(
                onDismissRequest = {
                    postIdToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            postIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(ProfileLoggedMviModel.Intent.DeletePost(itemId))
                            postIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonConfirm)
                    }
                },
                text = {
                    Text(text = LocalXmlStrings.current.messageAreYouSure)
                },
            )
        }
        commentIdToDelete?.also { itemId ->
            AlertDialog(
                onDismissRequest = {
                    commentIdToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            commentIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(ProfileLoggedMviModel.Intent.DeleteComment(itemId))
                            commentIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonConfirm)
                    }
                },
                text = {
                    Text(text = LocalXmlStrings.current.messageAreYouSure)
                },
            )
        }
    }
}
