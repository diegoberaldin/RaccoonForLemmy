package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ReportOff
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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeActionCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ReportListTypeSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostReportModel
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.components.CommentReportCard
import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.components.PostReportCard
import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.components.ReportCardPlaceHolder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

class ReportListScreen(
    private val communityId: Long? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ReportListMviModel> { parametersOf(communityId) }
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val navigationCoordinator = remember { getNavigationCoordinator() }
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val lazyListState = rememberLazyListState()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.refreshing,
            onRefresh = rememberCallback(model) {
                model.reduce(ReportListMviModel.Intent.Refresh)
            },
        )
        val detailOpener = remember { getDetailOpener() }
        val defaultResolveColor = MaterialTheme.colorScheme.secondary

        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    ReportListMviModel.Effect.BackToTop -> kotlin.runCatching {
                        lazyListState.scrollToItem(0)
                        topAppBarState.heightOffset = 0f
                        topAppBarState.contentOffset = 0f
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xxs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = {
                                    navigationCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Column(modifier = Modifier.padding(horizontal = Spacing.s)) {
                            Text(
                                text = LocalXmlStrings.current.reportListTitle,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            val text = when (uiState.unresolvedOnly) {
                                true -> LocalXmlStrings.current.reportListTypeUnresolved
                                else -> LocalXmlStrings.current.reportListTypeAll
                            }
                            Text(
                                modifier = Modifier.onClick(
                                    onClick = {
                                        val sheet = ReportListTypeSheet()
                                        navigationCoordinator.showBottomSheet(sheet)
                                    },
                                ),
                                text = text,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
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
                SectionSelector(
                    titles = listOf(
                        LocalXmlStrings.current.profileSectionPosts,
                        LocalXmlStrings.current.profileSectionComments,
                    ),
                    currentSection = when (uiState.section) {
                        ReportListSection.Comments -> 1
                        else -> 0
                    },
                    onSectionSelected = {
                        val section = when (it) {
                            1 -> ReportListSection.Comments
                            else -> ReportListSection.Posts
                        }
                        model.reduce(ReportListMviModel.Intent.ChangeSection(section))
                    },
                )

                Box(
                    modifier = Modifier
                        .then(
                            if (settings.hideNavigationBarWhileScrolling) {
                                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                Modifier
                            }
                        )
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.interItem)
                    ) {
                        if (uiState.section == ReportListSection.Posts) {
                            if (uiState.postReports.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    ReportCardPlaceHolder(uiState.postLayout)
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
                                    }
                                }
                            }
                            if (uiState.postReports.isEmpty() && !uiState.initial && !uiState.loading) {
                                item {
                                    Text(
                                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                        textAlign = TextAlign.Center,
                                        text = LocalXmlStrings.current.messageEmptyList,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                            items(
                                items = uiState.postReports,
                                key = {
                                    it.id.toString() + (it.updateDate
                                        ?: it.publishDate) + it.resolved + uiState.unresolvedOnly
                                },
                            ) { report ->
                                SwipeActionCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = uiState.swipeActionsEnabled,
                                    onGestureBegin = rememberCallback(model) {
                                        model.reduce(ReportListMviModel.Intent.HapticIndication)
                                    },
                                    swipeToStartActions = buildList {
                                        this += SwipeAction(
                                            swipeContent = {
                                                val icon = when {
                                                    report.resolved -> Icons.Default.Report
                                                    else -> Icons.Default.ReportOff
                                                }
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                )
                                            },
                                            backgroundColor = defaultResolveColor,
                                            onTriggered = rememberCallback {
                                                model.reduce(
                                                    ReportListMviModel.Intent.ResolvePost(report.id),
                                                )
                                            },
                                        )
                                    },
                                    content = {
                                        PostReportCard(
                                            report = report,
                                            postLayout = uiState.postLayout,
                                            autoLoadImages = uiState.autoLoadImages,
                                            preferNicknames = uiState.preferNicknames,
                                            onOpen = rememberCallback {
                                                detailOpener.openPostDetail(
                                                    post = PostModel(id = report.postId),
                                                    isMod = true,
                                                )
                                            },
                                            options = buildList {
                                                this += Option(
                                                    OptionId.SeeRaw,
                                                    LocalXmlStrings.current.postActionSeeRaw,
                                                )
                                                this += Option(
                                                    OptionId.ResolveReport,
                                                    if (report.resolved) {
                                                        LocalXmlStrings.current.reportActionUnresolve
                                                    } else {
                                                        LocalXmlStrings.current.reportActionResolve
                                                    },
                                                )
                                            },
                                            onOptionSelected = rememberCallbackArgs { optionId ->
                                                when (optionId) {
                                                    OptionId.SeeRaw -> {
                                                        rawContent = report
                                                    }

                                                    OptionId.ResolveReport -> {
                                                        model.reduce(
                                                            ReportListMviModel.Intent.ResolvePost(
                                                                report.id
                                                            )
                                                        )
                                                    }

                                                    else -> Unit
                                                }
                                            },
                                        )
                                    },
                                )
                                if (uiState.postLayout != PostLayout.Card) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.interItem))
                                }
                            }
                        } else {
                            if (uiState.commentReports.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    ReportCardPlaceHolder(uiState.postLayout)
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
                                    }
                                }
                            }
                            if (uiState.commentReports.isEmpty() && !uiState.initial && !uiState.loading) {
                                item {
                                    Text(
                                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                        textAlign = TextAlign.Center,
                                        text = LocalXmlStrings.current.messageEmptyList,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                            items(
                                uiState.commentReports,
                                { it.id.toString() + (it.updateDate ?: it.publishDate) },
                            ) { report ->
                                SwipeActionCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = uiState.swipeActionsEnabled,
                                    onGestureBegin = rememberCallback(model) {
                                        model.reduce(ReportListMviModel.Intent.HapticIndication)
                                    },
                                    swipeToStartActions = buildList {
                                        this += SwipeAction(
                                            swipeContent = {
                                                val icon = when {
                                                    report.resolved -> Icons.Default.Report
                                                    else -> Icons.Default.ReportOff
                                                }
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                )
                                            },
                                            backgroundColor = defaultResolveColor,
                                            onTriggered = rememberCallback {
                                                model.reduce(
                                                    ReportListMviModel.Intent.ResolveComment(report.id),
                                                )
                                            },
                                        )
                                    },
                                    content = {
                                        CommentReportCard(
                                            report = report,
                                            postLayout = uiState.postLayout,
                                            autoLoadImages = uiState.autoLoadImages,
                                            preferNicknames = uiState.preferNicknames,
                                            onOpen = rememberCallback {
                                                detailOpener.openPostDetail(
                                                    post = PostModel(id = report.postId),
                                                    highlightCommentId = report.commentId,
                                                    isMod = true,
                                                )
                                            },
                                            options = buildList {
                                                this += Option(
                                                    OptionId.SeeRaw,
                                                    LocalXmlStrings.current.postActionSeeRaw,
                                                )
                                                this += Option(
                                                    OptionId.ResolveReport,
                                                    if (report.resolved) {
                                                        LocalXmlStrings.current.reportActionUnresolve
                                                    } else {
                                                        LocalXmlStrings.current.reportActionResolve
                                                    },
                                                )
                                            },
                                            onOptionSelected = rememberCallbackArgs { optionId ->
                                                when (optionId) {
                                                    OptionId.SeeRaw -> {
                                                        rawContent = report
                                                    }

                                                    OptionId.ResolveReport -> {
                                                        model.reduce(
                                                            ReportListMviModel.Intent.ResolveComment(
                                                                report.id
                                                            )
                                                        )
                                                    }

                                                    else -> Unit
                                                }
                                            },
                                        )
                                    },
                                )
                                if (uiState.postLayout != PostLayout.Card) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.interItem))
                                }
                            }
                        }

                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(ReportListMviModel.Intent.LoadNextPage)
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
                            Spacer(modifier = Modifier.height(Spacing.xxxl))
                        }
                    }

                    if (uiState.asyncInProgress) {
                        ProgressHud()
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
                is PostReportModel -> {
                    RawContentDialog(
                        title = content.originalTitle,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        url = content.originalUrl,
                        text = content.originalText,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }

                is CommentReportModel -> {
                    RawContentDialog(
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        text = content.originalText,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }
            }
        }
    }
}
