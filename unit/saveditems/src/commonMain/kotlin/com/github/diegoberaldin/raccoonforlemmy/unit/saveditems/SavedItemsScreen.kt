package com.github.diegoberaldin.raccoonforlemmy.unit.saveditems

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ShareBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.launch

class SavedItemsScreen : Screen {

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<SavedItemsMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigatorCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val detailOpener = remember { getDetailOpener() }

        Scaffold(
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.navigationDrawerTitleBookmarks,
                        )
                    },
                    actions = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    val sheet = SortBottomSheet(
                                        values = uiState.availableSortTypes.map { it.toInt() },
                                    )
                                    navigatorCoordinator.showBottomSheet(sheet)
                                },
                            ),
                            imageVector = uiState.sortType.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    navigatorCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isFabVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it * 2 },
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it * 2 },
                    ),
                ) {
                    FloatingActionButtonMenu(
                        items = buildList {
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.ExpandLess,
                                text = LocalXmlStrings.current.actionBackToTop,
                                onSelected = rememberCallback {
                                    scope.launch {
                                        lazyListState.scrollToItem(0)
                                        topAppBarState.heightOffset = 0f
                                        topAppBarState.contentOffset = 0f
                                    }
                                },
                            )
                        },
                    )
                }
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues).then(
                    if (settings.hideNavigationBarWhileScrolling) {
                        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        Modifier
                    }
                ).nestedScroll(fabNestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                SectionSelector(
                    modifier = Modifier.padding(vertical = Spacing.xs),
                    titles = listOf(
                        LocalXmlStrings.current.profileSectionPosts,
                        LocalXmlStrings.current.profileSectionComments,
                    ),
                    currentSection = when (uiState.section) {
                        SavedItemsSection.Comments -> 1
                        else -> 0
                    },
                    onSectionSelected = {
                        val section = when (it) {
                            1 -> SavedItemsSection.Comments
                            else -> SavedItemsSection.Posts
                        }
                        model.reduce(SavedItemsMviModel.Intent.ChangeSection(section))
                    },
                )
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh = rememberCallback(model) {
                        model.reduce(SavedItemsMviModel.Intent.Refresh)
                    },
                )
                Box(
                    modifier = Modifier.fillMaxWidth().pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.padding(horizontal = Spacing.xxxs),
                    ) {
                        if (uiState.section == SavedItemsSection.Posts) {
                            items(uiState.posts) { post ->
                                PostCard(
                                    post = post,
                                    postLayout = uiState.postLayout,
                                    limitBodyHeight = true,
                                    fullHeightImage = uiState.fullHeightImages,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    preferNicknames = uiState.preferNicknames,
                                    showScores = uiState.showScores,
                                    blurNsfw = uiState.blurNsfw,
                                    onClick = rememberCallback {
                                        detailOpener.openPostDetail(post)
                                    },
                                    onOpenCommunity = rememberCallbackArgs { community, instance ->
                                        detailOpener.openCommunityDetail(community, instance)
                                    },
                                    onOpenCreator = rememberCallbackArgs { u, instance ->
                                        if (u.id != uiState.user?.id) {
                                            detailOpener.openUserDetail(u, instance)
                                        }
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
                                            SavedItemsMviModel.Intent.UpVotePost(
                                                id = post.id,
                                            ),
                                        )
                                    },
                                    onDownVote = rememberCallback(model) {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.DownVotePost(
                                                id = post.id,
                                            ),
                                        )
                                    },
                                    onSave = rememberCallback(model) {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.SavePost(
                                                id = post.id,
                                            ),
                                        )
                                    },
                                    onReply = rememberCallback {
                                        detailOpener.openPostDetail(post)
                                    },
                                    onOpenImage = rememberCallbackArgs { url ->
                                        navigatorCoordinator.pushScreen(
                                            ZoomableImageScreen(
                                                url = url,
                                                source = post.community?.readableHandle.orEmpty(),
                                            ),
                                        )
                                    },
                                    options = buildList {
                                        add(
                                            Option(
                                                OptionId.Share,
                                                LocalXmlStrings.current.postActionShare
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.SeeRaw,
                                                LocalXmlStrings.current.postActionSeeRaw
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Report,
                                                LocalXmlStrings.current.postActionReport
                                            )
                                        )
                                    },
                                    onOptionSelected = { optionIndex ->
                                        when (optionIndex) {
                                            OptionId.Report -> {
                                                navigatorCoordinator.pushScreen(
                                                    CreateReportScreen(postId = post.id),
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
                                                        SavedItemsMviModel.Intent.Share(
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
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.s))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.s))
                                }
                            }

                            if (uiState.posts.isEmpty() && !uiState.loading) {
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
                        } else {
                            items(uiState.comments) { comment ->
                                CommentCard(
                                    comment = comment,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    preferNicknames = uiState.preferNicknames,
                                    showScores = uiState.showScores,
                                    hideIndent = true,
                                    onClick = {
                                        detailOpener.openPostDetail(
                                            post = PostModel(id = comment.postId),
                                            highlightCommentId = comment.id,
                                        )
                                    },
                                    onImageClick = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(
                                            ZoomableImageScreen(
                                                url = url,
                                                source = comment.community?.readableHandle.orEmpty(),
                                            )
                                        )
                                    },
                                    onUpVote = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.UpVoteComment(
                                                id = comment.id,
                                            ),
                                        )
                                    },
                                    onDownVote = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.DownVoteComment(
                                                id = comment.id,
                                            ),
                                        )
                                    },
                                    onSave = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.SaveComment(
                                                id = comment.id,
                                            ),
                                        )
                                    },
                                    onReply = {
                                        detailOpener.openReply(
                                            originalPost = PostModel(id = comment.postId),
                                            originalComment = comment,
                                        )

                                    },
                                    options = buildList {
                                        add(
                                            Option(
                                                OptionId.SeeRaw,
                                                LocalXmlStrings.current.postActionSeeRaw
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Report,
                                                LocalXmlStrings.current.postActionReport
                                            )
                                        )
                                    },
                                    onOptionSelected = { optionIndex ->
                                        when (optionIndex) {
                                            OptionId.Report -> {
                                                navigatorCoordinator.pushScreen(
                                                    CreateReportScreen(commentId = comment.id),
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

                            if (uiState.comments.isEmpty() && !uiState.loading) {
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
                                model.reduce(SavedItemsMviModel.Intent.LoadNextPage)
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
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        upVotes = content.upvotes,
                        downVotes = content.downvotes,
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
                        }
                    )
                }
            }
        }
    }
}
