package com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.posts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeActionCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.ModeratorZoneAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.comments.components.ModdedCommentPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen

class ModdedPostsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ModdedPostsMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val lazyListState = rememberLazyListState()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.refreshing,
            onRefresh = rememberCallback(model) {
                model.reduce(ModdedPostsMviModel.Intent.Refresh)
            },
        )
        val detailOpener = remember { getDetailOpener() }
        var rawContent by remember { mutableStateOf<PostModel?>(null) }
        val themeRepository = remember { getThemeRepository() }
        val upVoteColor by themeRepository.upVoteColor.collectAsState()
        val downVoteColor by themeRepository.downVoteColor.collectAsState()
        val replyColor by themeRepository.replyColor.collectAsState()
        val saveColor by themeRepository.saveColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultReplyColor = MaterialTheme.colorScheme.secondary
        val defaultSaveColor = MaterialTheme.colorScheme.secondaryContainer
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xxs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    navigationCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Text(
                            text = ModeratorZoneAction.ModeratedPosts.toReadableName(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues).then(
                    if (settings.hideNavigationBarWhileScrolling) {
                        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        Modifier
                    }
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Box(
                    modifier = Modifier.then(
                        if (settings.hideNavigationBarWhileScrolling) {
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            Modifier
                        }
                    ).pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                    ) {
                        if (uiState.posts.isEmpty() && uiState.loading && uiState.initial) {
                            items(5) {
                                ModdedCommentPlaceholder(uiState.postLayout)
                                if (uiState.postLayout != PostLayout.Card) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.s))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.s))
                                }
                            }
                        }
                        if (uiState.posts.isEmpty() && !uiState.initial && !uiState.loading) {
                            item {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    text = LocalXmlStrings.current.messageEmptyList,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }

                        items(
                            items = uiState.posts,
                            key = { it.id.toString() + (it.updateDate ?: it.publishDate) },
                        ) { post ->

                            @Composable
                            fun List<ActionOnSwipe>.toSwipeActions(): List<SwipeAction> =
                                mapNotNull {
                                    when (it) {
                                        ActionOnSwipe.UpVote -> SwipeAction(
                                            swipeContent = {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowCircleUp,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                )
                                            },
                                            backgroundColor = upVoteColor ?: defaultUpvoteColor,
                                            onTriggered = rememberCallback {
                                                model.reduce(
                                                    ModdedPostsMviModel.Intent.UpVotePost(
                                                        post.id
                                                    )
                                                )
                                            },
                                        )

                                        ActionOnSwipe.DownVote -> SwipeAction(
                                            swipeContent = {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowCircleDown,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                )
                                            },
                                            backgroundColor = downVoteColor ?: defaultDownVoteColor,
                                            onTriggered = rememberCallback {
                                                model.reduce(
                                                    ModdedPostsMviModel.Intent.DownVotePost(
                                                        post.id
                                                    )
                                                )
                                            },
                                        )

                                        ActionOnSwipe.Reply -> SwipeAction(
                                            swipeContent = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                )
                                            },
                                            backgroundColor = replyColor ?: defaultReplyColor,
                                            onTriggered = rememberCallback {
                                                detailOpener.openReply(originalPost = post)
                                            },
                                        )

                                        ActionOnSwipe.Save -> SwipeAction(
                                            swipeContent = {
                                                Icon(
                                                    imageVector = Icons.Default.Bookmark,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                )
                                            },
                                            backgroundColor = saveColor ?: defaultSaveColor,
                                            onTriggered = rememberCallback {
                                                model.reduce(
                                                    ModdedPostsMviModel.Intent.SavePost(
                                                        post.id
                                                    )
                                                )
                                            },
                                        )


                                        else -> null
                                    }
                                }

                            SwipeActionCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.swipeActionsEnabled,
                                onGestureBegin = rememberCallback(model) {
                                    model.reduce(ModdedPostsMviModel.Intent.HapticIndication)
                                },
                                swipeToStartActions = uiState.actionsOnSwipeToStartPosts.toSwipeActions(),
                                swipeToEndActions = uiState.actionsOnSwipeToEndPosts.toSwipeActions(),
                                content = {
                                    PostCard(
                                        post = post,
                                        postLayout = uiState.postLayout,
                                        limitBodyHeight = true,
                                        fullHeightImage = uiState.fullHeightImages,
                                        voteFormat = uiState.voteFormat,
                                        autoLoadImages = uiState.autoLoadImages,
                                        preferNicknames = uiState.preferNicknames,
                                        onClick = rememberCallback(model) {
                                            detailOpener.openPostDetail(post)
                                        },
                                        onOpenCommunity = rememberCallbackArgs { community, instance ->
                                            detailOpener.openCommunityDetail(
                                                community,
                                                instance,
                                            )
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
                                        onUpVote = rememberCallback(model) {
                                            model.reduce(
                                                ModdedPostsMviModel.Intent.UpVotePost(
                                                    id = post.id,
                                                ),
                                            )
                                        },
                                        onDownVote = rememberCallback(model) {
                                            model.reduce(
                                                ModdedPostsMviModel.Intent.DownVotePost(
                                                    id = post.id,
                                                ),
                                            )
                                        },
                                        onSave = rememberCallback(model) {
                                            model.reduce(
                                                ModdedPostsMviModel.Intent.SavePost(
                                                    id = post.id,
                                                ),
                                            )
                                        },
                                        onReply = rememberCallback(model) {
                                            detailOpener.openPostDetail(post)
                                        },
                                        onOpenImage = rememberCallbackArgs(model, post) { url ->
                                            navigationCoordinator.pushScreen(
                                                ZoomableImageScreen(
                                                    url = url,
                                                    source = post.community?.readableHandle.orEmpty(),
                                                )
                                            )
                                        },
                                        options = buildList {
                                            this += Option(
                                                OptionId.Remove,
                                                LocalXmlStrings.current.modActionRemove,
                                            )
                                            this += Option(
                                                OptionId.SeeRaw,
                                                LocalXmlStrings.current.postActionSeeRaw,
                                            )
                                            this += Option(
                                                OptionId.FeaturePost,
                                                if (post.featuredCommunity) {
                                                    LocalXmlStrings.current.modActionUnmarkAsFeatured
                                                } else {
                                                    LocalXmlStrings.current.modActionMarkAsFeatured
                                                },
                                            )
                                            this += Option(
                                                OptionId.LockPost,
                                                if (post.locked) {
                                                    LocalXmlStrings.current.modActionUnlock
                                                } else {
                                                    LocalXmlStrings.current.modActionLock
                                                },
                                            )
                                            this += Option(
                                                OptionId.BanUser,
                                                if (post.creator?.banned == true) {
                                                    LocalXmlStrings.current.modActionAllow
                                                } else {
                                                    LocalXmlStrings.current.modActionBan
                                                },
                                            )
                                        },
                                        onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                            when (optionId) {
                                                OptionId.SeeRaw -> {
                                                    rawContent = post
                                                }

                                                OptionId.FeaturePost -> model.reduce(
                                                    ModdedPostsMviModel.Intent.ModFeaturePost(
                                                        post.id
                                                    )
                                                )

                                                OptionId.LockPost -> model.reduce(
                                                    ModdedPostsMviModel.Intent.ModLockPost(post.id)
                                                )

                                                OptionId.Remove -> {
                                                    val screen = RemoveScreen(postId = post.id)
                                                    navigationCoordinator.pushScreen(screen)
                                                }

                                                OptionId.BanUser -> {
                                                    post.creator?.id?.also { userId ->
                                                        post.community?.id?.also { communityId ->
                                                            val screen = BanUserScreen(
                                                                userId = userId,
                                                                communityId = communityId,
                                                                newValue = post.creator?.banned != true,
                                                                postId = post.id,
                                                            )
                                                            navigationCoordinator.pushScreen(screen)
                                                        }
                                                    }
                                                }

                                                else -> Unit
                                            }
                                        }
                                    )
                                },
                            )
                            if (uiState.postLayout != PostLayout.Card) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.s))
                            } else {
                                Spacer(modifier = Modifier.height(Spacing.s))
                            }
                        }

                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(ModdedPostsMviModel.Intent.LoadNextPage)
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

                        item {
                            Spacer(modifier = Modifier.height(Spacing.xxl))
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
                        }
                    )
                }
            }
        }
    }
}