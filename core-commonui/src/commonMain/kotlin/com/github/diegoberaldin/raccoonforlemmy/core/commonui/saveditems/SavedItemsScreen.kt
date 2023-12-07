package com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createreport.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getSavedItemsViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

class SavedItemsScreen : Screen {

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getSavedItemsViewModel() }
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

        Scaffold(
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = stringResource(MR.strings.navigation_drawer_title_bookmarks),
                        )
                    },
                    actions = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    val sheet = SortBottomSheet(
                                        sheetKey = key,
                                        comments = false,
                                        values = uiState.availableSortTypes,
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
                            imageVector = Icons.Default.ArrowBack,
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
                    FloatingActionButtonMenu(items = buildList {
                        this += FloatingActionButtonMenuItem(
                            icon = Icons.Default.ExpandLess,
                            text = stringResource(MR.strings.action_back_to_top),
                            onSelected = rememberCallback {
                                scope.launch {
                                    lazyListState.scrollToItem(0)
                                    topAppBarState.heightOffset = 0f
                                    topAppBarState.contentOffset = 0f
                                }
                            },
                        )
                    })
                }
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues).let {
                    if (settings.hideNavigationBarWhileScrolling) {
                        it.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        it
                    }
                }.nestedScroll(fabNestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                SectionSelector(
                    modifier = Modifier.padding(vertical = Spacing.s),
                    titles = listOf(
                        stringResource(MR.strings.profile_section_posts),
                        stringResource(MR.strings.profile_section_comments),
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
                                    fullHeightImage = uiState.fullHeightImages,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    blurNsfw = uiState.blurNsfw,
                                    onClick = rememberCallback {
                                        navigatorCoordinator.pushScreen(
                                            PostDetailScreen(post),
                                        )
                                    },
                                    onOpenCommunity = rememberCallbackArgs { community ->
                                        navigatorCoordinator.pushScreen(
                                            CommunityDetailScreen(community),
                                        )
                                    },
                                    onOpenCreator = rememberCallbackArgs { u ->
                                        if (u.id != uiState.user?.id) {
                                            navigatorCoordinator.pushScreen(UserDetailScreen(u))
                                        }
                                    },
                                    onUpVote = rememberCallback(model) {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.UpVotePost(
                                                id = post.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onDownVote = rememberCallback(model) {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.DownVotePost(
                                                id = post.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onSave = rememberCallback(model) {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.SavePost(
                                                id = post.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onReply = rememberCallback {
                                        navigationCoordinator.pushScreen(
                                            PostDetailScreen(post),
                                        )
                                    },
                                    onImageClick = rememberCallbackArgs { url ->
                                        navigatorCoordinator.pushScreen(
                                            ZoomableImageScreen(url),
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
                                                OptionId.Report,
                                                stringResource(MR.strings.post_action_report)
                                            )
                                        )
                                    },
                                    onOptionSelected = { optionIndex ->
                                        when (optionIndex) {
                                            OptionId.Report -> {
                                                navigatorCoordinator.showBottomSheet(
                                                    CreateReportScreen(
                                                        postId = post.id
                                                    )
                                                )
                                            }

                                            OptionId.SeeRaw -> {
                                                rawContent = post
                                            }

                                            OptionId.Share -> {
                                                model.reduce(
                                                    SavedItemsMviModel.Intent.SharePost(
                                                        post.id
                                                    )
                                                )
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

                            if (uiState.posts.isEmpty() && !uiState.loading) {
                                item {
                                    androidx.compose.material.Text(
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
                            items(uiState.comments) { comment ->
                                CommentCard(
                                    comment = comment,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    hideIndent = true,
                                    onClick = {
                                        navigatorCoordinator.pushScreen(
                                            PostDetailScreen(
                                                post = PostModel(id = comment.postId),
                                                highlightCommentId = comment.id,
                                            ),
                                        )
                                    },
                                    onUpVote = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.UpVoteComment(
                                                id = comment.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onDownVote = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.DownVoteComment(
                                                id = comment.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onSave = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.SaveComment(
                                                id = comment.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onReply = {
                                        val screen = CreateCommentScreen(
                                            originalPost = PostModel(id = comment.postId),
                                            originalComment = comment,
                                        )
                                        navigatorCoordinator.showBottomSheet(screen)
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
                                                OptionId.Report,
                                                stringResource(MR.strings.post_action_report)
                                            )
                                        )
                                    },
                                    onOptionSelected = { optionIndex ->
                                        when (optionIndex) {
                                            OptionId.Report -> {
                                                navigatorCoordinator.showBottomSheet(
                                                    CreateReportScreen(
                                                        commentId = comment.id
                                                    )
                                                )
                                            }

                                            OptionId.SeeRaw -> {
                                                rawContent = comment
                                            }

                                            else -> Unit
                                        }
                                    },
                                )
                                Divider(
                                    modifier = Modifier.padding(vertical = Spacing.xxxs),
                                    thickness = 0.25.dp
                                )
                            }

                            if (uiState.comments.isEmpty() && !uiState.loading) {
                                item {
                                    androidx.compose.material.Text(
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
                        onDismiss = {
                            rawContent = null
                        },
                        onQuote = { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                val screen = CreateCommentScreen(
                                    originalPost = content,
                                    initialText = buildString {
                                        append("> ")
                                        append(quotation)
                                        append("\n\n")
                                    })
                                navigationCoordinator.showBottomSheet(screen)
                            }
                        })
                }

                is CommentModel -> {
                    RawContentDialog(
                        text = content.text,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        onDismiss = {
                            rawContent = null
                        },
                        onQuote = { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                val screen = CreateCommentScreen(originalComment = content,
                                    initialText = buildString {
                                        append("> ")
                                        append(quotation)
                                        append("\n\n")
                                    })
                                navigationCoordinator.showBottomSheet(screen)
                            }
                        }
                    )
                }
            }
        }
    }
}
