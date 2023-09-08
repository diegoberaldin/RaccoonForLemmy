package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.racconforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.Dropdown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCommunityDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

class CommunityDetailScreen(
    private val community: CommunityModel,
    private val otherInstance: String = "",
    private val onBack: () -> Unit,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel(community.id.toString() + otherInstance) {
            getCommunityDetailViewModel(
                community = community,
                otherInstance = otherInstance,
            )
        }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val isOnOtherInstance = otherInstance.isNotEmpty()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val isFabVisible = remember { mutableStateOf(true) }
        val fabNestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y < -1) {
                        isFabVisible.value = false
                    }
                    if (available.y > 1) {
                        isFabVisible.value = true
                    }
                    return Offset.Zero
                }
            }
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(Spacing.xs),
            topBar = {
                val communityName = community.name
                val communityHost = community.host
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = buildString {
                                append(communityName)
                                if (communityHost.isNotEmpty()) {
                                    append("@$communityHost")
                                }
                            },
                        )
                    },
                    actions = {
                        Image(
                            modifier = Modifier.onClick {
                                bottomSheetNavigator.show(
                                    SortBottomSheet(
                                        expandTop = true,
                                        onSelected = {
                                            model.reduce(
                                                CommunityDetailMviModel.Intent.ChangeSort(
                                                    it,
                                                ),
                                            )
                                        },
                                        onHide = {
                                            bottomSheetNavigator.hide()
                                        },
                                    ),
                                )
                            },
                            imageVector = uiState.sortType.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick {
                                onBack()
                            },
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        )
                    },
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isFabVisible.value,
                    enter = slideInVertically(
                        initialOffsetY = { it * 2 },
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it * 2 },
                    ),
                ) {
                    FloatingActionButton(
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape,
                        onClick = {
                            bottomSheetNavigator.show(
                                CreatePostScreen(
                                    communityId = community.id,
                                    onPostCreated = {
                                        bottomSheetNavigator.hide()
                                        model.reduce(CommunityDetailMviModel.Intent.Refresh)
                                    }
                                )
                            )
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        ) { padding ->
            val community = uiState.community
            val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                model.reduce(CommunityDetailMviModel.Intent.Refresh)
            })
            Box(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(fabNestedScrollConnection)
                    .padding(padding)
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    item {
                        val communityIcon = community.icon.orEmpty()
                        val communityTitle = community.title

                        val iconSize = 80.dp
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            var width by remember { mutableStateOf(0.dp) }
                            var height by remember { mutableStateOf(0.dp) }
                            var optionsExpanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier.onGloballyPositioned {
                                    width = it.size.width.dp
                                    height = it.size.height.dp
                                },
                            ) {
                                val banner = community.banner.orEmpty()
                                if (banner.isNotEmpty()) {
                                    val painterResource = asyncPainterResource(banner)
                                    KamelImage(
                                        modifier = Modifier.fillMaxWidth().aspectRatio(2f),
                                        resource = painterResource,
                                        contentScale = ContentScale.FillBounds,
                                        contentDescription = null,
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().aspectRatio(2f),
                                    )
                                }
                                Icon(
                                    modifier = Modifier
                                        .padding(
                                            top = Spacing.s,
                                            end = Spacing.s,
                                        )
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape,
                                        )
                                        .padding(Spacing.s)
                                        .align(Alignment.TopEnd)
                                        .onClick {
                                            optionsExpanded = true
                                        },
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                                Dropdown(
                                    expanded = optionsExpanded,
                                    onDismiss = {
                                        optionsExpanded = false
                                    },
                                    offset = DpOffset(
                                        x = width - Spacing.m,
                                        y = -height,
                                    ),
                                ) {
                                    Text(
                                        modifier = Modifier.padding(
                                            horizontal = Spacing.m,
                                            vertical = Spacing.xs,
                                        ).onClick {
                                            optionsExpanded = false
                                            bottomSheetNavigator.show(
                                                CommunityInfoScreen(community),
                                            )
                                        },
                                        text = stringResource(MR.strings.community_detail_info),
                                    )
                                    Text(
                                        modifier = Modifier.padding(
                                            horizontal = Spacing.m,
                                            vertical = Spacing.xs,
                                        ).onClick {
                                            optionsExpanded = false
                                            navigator.push(
                                                InstanceInfoScreen(
                                                    url = community.instanceUrl,
                                                    onBack = {
                                                        navigator.pop()
                                                    },
                                                ),
                                            )
                                        },
                                        text = stringResource(MR.strings.community_detail_instance_info),
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.graphicsLayer(translationY = -(iconSize / 2).toLocalPixel()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                            ) {
                                if (communityIcon.isNotEmpty()) {
                                    val painterResource =
                                        asyncPainterResource(data = communityIcon)
                                    KamelImage(
                                        modifier = Modifier.padding(Spacing.xxxs).size(iconSize)
                                            .clip(RoundedCornerShape(iconSize / 2)),
                                        resource = painterResource,
                                        contentDescription = null,
                                        contentScale = ContentScale.FillBounds,
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.padding(Spacing.xxxs).size(iconSize)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(iconSize / 2),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = community.name.firstOrNull()?.toString()
                                                .orEmpty().uppercase(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }
                                Text(
                                    text = buildString {
                                        append(communityTitle)
                                    },
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                                if (!isOnOtherInstance) {
                                    Button(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(top = Spacing.m),
                                        onClick = {
                                            when (community.subscribed) {
                                                true -> model.reduce(CommunityDetailMviModel.Intent.Unsubscribe)
                                                false -> model.reduce(CommunityDetailMviModel.Intent.Subscribe)
                                                else -> Unit
                                            }
                                        },
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Image(
                                                imageVector = when (community.subscribed) {
                                                    true -> Icons.Default.Check
                                                    false -> Icons.Default.AddCircle
                                                    else -> Icons.Default.MoreHoriz
                                                },
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onPrimary),
                                            )
                                            Text(
                                                text = when (community.subscribed) {
                                                    true -> stringResource(MR.strings.community_button_subscribed)
                                                    false -> stringResource(MR.strings.community_button_subscribe)
                                                    else -> stringResource(MR.strings.community_button_pending)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    items(uiState.posts) { post ->
                        SwipeableCard(
                            modifier = Modifier.fillMaxWidth(),
                            directions = if (isOnOtherInstance) {
                                emptySet()
                            } else {
                                setOf(
                                    DismissDirection.StartToEnd,
                                    DismissDirection.EndToStart,
                                )
                            },
                            backgroundColor = {
                                when (it) {
                                    DismissValue.DismissedToStart -> MaterialTheme.colorScheme.secondary
                                    DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.tertiary
                                    else -> Color.Transparent
                                }
                            },
                            swipeContent = { direction ->
                                val icon = when (direction) {
                                    DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                    DismissDirection.EndToStart -> Icons.Default.ArrowCircleUp
                                }
                                val (iconModifier, iconTint) = when {
                                    direction == DismissDirection.StartToEnd && post.myVote < 0 -> {
                                        Modifier.background(
                                            color = Color.Transparent,
                                            shape = CircleShape,
                                        ) to MaterialTheme.colorScheme.onTertiary
                                    }

                                    direction == DismissDirection.StartToEnd -> {
                                        Modifier.background(
                                            color = MaterialTheme.colorScheme.onTertiary,
                                            shape = CircleShape,
                                        ) to MaterialTheme.colorScheme.tertiary
                                    }

                                    direction == DismissDirection.EndToStart && post.myVote > 0 -> {
                                        Modifier.background(
                                            color = Color.Transparent,
                                            shape = CircleShape,
                                        ) to MaterialTheme.colorScheme.onSecondary
                                    }

                                    else -> {
                                        Modifier.background(
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            shape = CircleShape,
                                        ) to MaterialTheme.colorScheme.secondary
                                    }
                                }
                                Icon(
                                    modifier = iconModifier,
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                )
                            },
                            onGestureBegin = {
                                model.reduce(CommunityDetailMviModel.Intent.HapticIndication)
                            },
                            onDismissToStart = {
                                model.reduce(
                                    CommunityDetailMviModel.Intent.UpVotePost(
                                        post = post,
                                    ),
                                )
                            },
                            onDismissToEnd = {
                                model.reduce(
                                    CommunityDetailMviModel.Intent.DownVotePost(
                                        post = post,
                                    ),
                                )
                            },
                            content = {
                                PostCard(
                                    modifier = Modifier.onClick {
                                        navigator.push(
                                            PostDetailScreen(
                                                post = post,
                                                onBack = {
                                                    navigator.pop()
                                                },
                                            ),
                                        )
                                    },
                                    onOpenCreator = { user ->
                                        navigator.push(
                                            UserDetailScreen(
                                                user = user,
                                                onBack = {
                                                    navigator.pop()
                                                },
                                            ),
                                        )
                                    },
                                    post = post,
                                    blurNsfw = when {
                                        community.nsfw -> false
                                        else -> uiState.blurNsfw
                                    },
                                    onUpVote = if (isOnOtherInstance) {
                                        null
                                    } else {
                                        {
                                            model.reduce(
                                                CommunityDetailMviModel.Intent.UpVotePost(
                                                    post = post,
                                                    feedback = true,
                                                ),
                                            )
                                        }
                                    },
                                    onDownVote = if (isOnOtherInstance) {
                                        null
                                    } else {
                                        {
                                            model.reduce(
                                                CommunityDetailMviModel.Intent.DownVotePost(
                                                    post = post,
                                                    feedback = true,
                                                ),
                                            )
                                        }
                                    },
                                    onSave = if (isOnOtherInstance) {
                                        null
                                    } else {
                                        {
                                            model.reduce(
                                                CommunityDetailMviModel.Intent.SavePost(
                                                    post = post,
                                                    feedback = true,
                                                ),
                                            )
                                        }
                                    },
                                    onReply = {
                                        bottomSheetNavigator.show(
                                            CreateCommentScreen(
                                                originalPost = post,
                                                onCommentCreated = {
                                                    bottomSheetNavigator.hide()
                                                    model.reduce(CommunityDetailMviModel.Intent.Refresh)
                                                }
                                            )
                                        )
                                    }
                                )
                            },
                        )
                    }
                    item {
                        if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                            model.reduce(CommunityDetailMviModel.Intent.LoadNextPage)
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
                        Spacer(modifier = Modifier.height(Spacing.s))
                    }
                }

                PullRefreshIndicator(
                    refreshing = uiState.refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
