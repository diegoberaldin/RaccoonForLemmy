package com.github.diegoberaldin.raccoonforlemmy.unit.drafts

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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.unit.drafts.components.DraftCard
import com.github.diegoberaldin.raccoonforlemmy.unit.drafts.components.DraftCardPlaceHolder

class DraftsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<DraftsMviModel>()
        val uiState by model.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val lazyListState = rememberLazyListState()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.refreshing,
            onRefresh = rememberCallback(model) {
                model.reduce(DraftsMviModel.Intent.Refresh)
            },
        )
        val detailOpener = remember { getDetailOpener() }
        var itemToDelete by remember { mutableStateOf<DraftModel?>(null) }

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
                        Text(
                            text = LocalXmlStrings.current.navigationDrawerTitleDrafts,
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
                SectionSelector(
                    modifier = Modifier.padding(vertical = Spacing.xs),
                    titles = listOf(
                        LocalXmlStrings.current.profileSectionPosts,
                        LocalXmlStrings.current.profileSectionComments,
                    ),
                    currentSection = when (uiState.section) {
                        DraftsSection.Comments -> 1
                        else -> 0
                    },
                    onSectionSelected = {
                        val section = when (it) {
                            1 -> DraftsSection.Comments
                            else -> DraftsSection.Posts
                        }
                        model.reduce(DraftsMviModel.Intent.ChangeSection(section))
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
                        modifier = Modifier.padding(horizontal = Spacing.xs),
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.interItem)
                    ) {
                        if (uiState.section == DraftsSection.Posts) {
                            if (uiState.postDrafts.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    DraftCardPlaceHolder(uiState.postLayout)
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
                                    }
                                }
                            }
                            if (uiState.postDrafts.isEmpty() && !uiState.initial) {
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
                                items = uiState.postDrafts,
                                key = { it.toKey() },
                            ) { draft ->
                                DraftCard(
                                    draft = draft,
                                    postLayout = uiState.postLayout,
                                    onOpen = rememberCallback {
                                        detailOpener.openCreatePost(
                                            draftId = draft.id,
                                            communityId = draft.communityId,
                                            initialText = draft.body,
                                            initialTitle = draft.title,
                                            initialUrl = draft.url,
                                            initialNsfw = draft.nsfw,
                                            forceCommunitySelection = true,
                                        )
                                    },
                                    options = buildList {
                                        this += Option(
                                            OptionId.Delete,
                                            LocalXmlStrings.current.commentActionDelete,
                                        )
                                    },
                                    onOptionSelected = rememberCallbackArgs { optionId ->
                                        when (optionId) {
                                            OptionId.Delete -> {
                                                draft.id?.also {
                                                    itemToDelete = draft
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
                        } else {
                            if (uiState.commentDrafts.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    DraftCardPlaceHolder(uiState.postLayout)
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
                                    }
                                }
                            }
                            if (uiState.commentDrafts.isEmpty() && !uiState.initial) {
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
                                uiState.commentDrafts,
                                key = { it.toKey() },
                            ) { draft ->
                                DraftCard(
                                    draft = draft,
                                    postLayout = uiState.postLayout,
                                    onOpen = rememberCallback {
                                        detailOpener.openReply(
                                            draftId = draft.id,
                                            originalPost = draft.postId?.let {
                                                PostModel(id = it)
                                            },
                                            originalComment = draft.parentId?.let {
                                                CommentModel(id = it, text = "")
                                            },
                                            initialText = draft.body,
                                        )
                                    },
                                    options = buildList {
                                        this += Option(
                                            OptionId.Delete,
                                            LocalXmlStrings.current.commentActionDelete,
                                        )
                                    },
                                    onOptionSelected = rememberCallbackArgs { optionId ->
                                        when (optionId) {
                                            OptionId.Delete -> {
                                                draft.id?.also {
                                                    itemToDelete = draft
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
                        }

                        item {
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

        itemToDelete?.also { item ->
            AlertDialog(
                onDismissRequest = {
                    itemToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            itemToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(DraftsMviModel.Intent.Delete(item))
                            itemToDelete = null
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

private fun DraftModel.toKey(): String = buildString {
    append(date)
    if (communityId != null) {
        append("&community=")
        append(communityId)
    } else {
        if (postId != null) {
            append("&post=")
            append(postId)
        } else if (parentId != null) {
            append("&comment=")
            append(parentId)
        }
    }
    if (!title.isNullOrEmpty()) {
        append("&title=")
        append(title)
    }
    append("&body=")
    append(body)
}
