package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.videoUrl

@Composable
fun PostCard(
    modifier: Modifier = Modifier,
    post: PostModel,
    isFromModerator: Boolean = false,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    showScores: Boolean = true,
    hideAuthor: Boolean = false,
    postLayout: PostLayout = PostLayout.Card,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    includeFullBody: Boolean = false,
    fullHeightImage: Boolean = true,
    limitBodyHeight: Boolean = false,
    blurNsfw: Boolean = true,
    actionButtonsActive: Boolean = true,
    options: List<Option> = emptyList(),
    onOpenCommunity: ((CommunityModel, String) -> Unit)? = null,
    onOpenCreator: ((UserModel, String) -> Unit)? = null,
    onOpenPost: ((PostModel, String) -> Unit)? = null,
    onOpenWeb: ((String) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOpenImage: ((String) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.then(
            if (postLayout == PostLayout.Card) {
                Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(CornerSize.l)
                    )
                    .padding(horizontal = Spacing.xs)
                    .clip(RoundedCornerShape(CornerSize.l))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                    )
                    .padding(Spacing.s)
            } else {
                Modifier
            }
        ).onClick(
            onClick = onClick ?: {},
            onDoubleClick = onDoubleClick ?: {},
        ),
    ) {
        if (postLayout != PostLayout.Compact) {
            ExtendedPost(
                post = post,
                isFromModerator = isFromModerator,
                hideAuthor = hideAuthor,
                backgroundColor = when (postLayout) {
                    PostLayout.Card -> MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
                    else -> MaterialTheme.colorScheme.background
                },
                showBody = includeFullBody || postLayout == PostLayout.Full,
                limitBodyHeight = limitBodyHeight,
                voteFormat = voteFormat,
                autoLoadImages = autoLoadImages,
                preferNicknames = preferNicknames,
                showScores = showScores,
                roundedCornerImage = postLayout == PostLayout.Card,
                fullHeightImage = fullHeightImage,
                blurNsfw = blurNsfw,
                actionButtonsActive = actionButtonsActive,
                options = options,
                onOpenCommunity = onOpenCommunity,
                onOpenCreator = onOpenCreator,
                onOpenPost = onOpenPost,
                onOpenWeb = onOpenWeb,
                onUpVote = onUpVote,
                onDownVote = onDownVote,
                onSave = onSave,
                onReply = onReply,
                onOpenImage = onOpenImage,
                onOptionSelected = onOptionSelected,
                onClick = onClick,
                onDoubleClick = onDoubleClick,
            )
        } else {
            CompactPost(
                post = post,
                isFromModerator = isFromModerator,
                hideAuthor = hideAuthor,
                blurNsfw = blurNsfw,
                voteFormat = voteFormat,
                autoLoadImages = autoLoadImages,
                preferNicknames = preferNicknames,
                showScores = showScores,
                actionButtonsActive = actionButtonsActive,
                options = options,
                onOpenCommunity = onOpenCommunity,
                onOpenCreator = onOpenCreator,
                onOpenPost = onOpenPost,
                onOpenWeb = onOpenWeb,
                onUpVote = onUpVote,
                onDownVote = onDownVote,
                onSave = onSave,
                onReply = onReply,
                onOpenImage = onOpenImage,
                onOptionSelected = onOptionSelected,
                onClick = onClick,
                onDoubleClick = onDoubleClick,
            )
        }
    }
}

@Composable
private fun CompactPost(
    modifier: Modifier = Modifier,
    post: PostModel,
    isFromModerator: Boolean = false,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    showScores: Boolean = true,
    hideAuthor: Boolean,
    blurNsfw: Boolean,
    actionButtonsActive: Boolean = true,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    options: List<Option> = emptyList(),
    onOpenCommunity: ((CommunityModel, String) -> Unit)? = null,
    onOpenCreator: ((UserModel, String) -> Unit)? = null,
    onOpenPost: ((PostModel, String) -> Unit)? = null,
    onOpenWeb: ((String) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOpenImage: ((String) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
) {
    val optionsMenuOpen = remember { mutableStateOf(false) }
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        optionsMenuOpen.value = true
                    },
                    onTap = {
                        onClick?.invoke()
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        CommunityAndCreatorInfo(
            community = post.community,
            creator = post.creator.takeIf { !hideAuthor },
            featured = post.featuredCommunity,
            featuredLocal = post.featuredLocal,
            locked = post.locked,
            isFromModerator = isFromModerator,
            onOpenCommunity = rememberCallbackArgs { community ->
                onOpenCommunity?.invoke(community, "")
            },
            onOpenCreator = rememberCallbackArgs { user ->
                onOpenCreator?.invoke(user, "")
            },
            autoLoadImages = autoLoadImages,
            preferNicknames = preferNicknames,
            onDoubleClick = onDoubleClick,
            onLongClick = rememberCallback {
                optionsMenuOpen.value = true
            },
        )
        Row(
            modifier = Modifier.padding(horizontal = Spacing.s),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            CustomizedContent(ContentFontClass.Title) {
                PostCardTitle(
                    modifier = Modifier.weight(0.75f),
                    text = post.title,
                    autoLoadImages = autoLoadImages,
                    onClick = onClick,
                    onOpenCommunity = onOpenCommunity,
                    onOpenUser = onOpenCreator,
                    onOpenPost = onOpenPost,
                    onOpenImage = onOpenImage,
                    onDoubleClick = onDoubleClick,
                    onOpenWeb = onOpenWeb,
                    onLongClick = {
                        optionsMenuOpen.value = true
                    },
                )
            }

            if (post.videoUrl.isNotEmpty()) {
                PostCardVideo(
                    modifier = Modifier.padding(vertical = Spacing.xxs),
                    url = post.videoUrl,
                    blurred = blurNsfw && post.nsfw,
                    autoLoadImages = autoLoadImages,
                    onOpen = onClick,
                )
            } else {
                PostCardImage(
                    modifier = Modifier
                        .weight(0.25f)
                        .clip(RoundedCornerShape(CornerSize.s)),
                    minHeight = Dp.Unspecified,
                    maxHeight = Dp.Unspecified,
                    imageUrl = post.imageUrl,
                    autoLoadImages = autoLoadImages,
                    loadButtonContent = @Composable {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    },
                    blurred = blurNsfw && post.nsfw,
                    onImageClick = onOpenImage,
                    onDoubleClick = onDoubleClick,
                    onLongClick = {
                        optionsMenuOpen.value = true
                    },
                )
            }
        }
        PostCardFooter(
            modifier = Modifier.padding(top = Spacing.xxs),
            comments = post.comments,
            voteFormat = voteFormat,
            score = post.score,
            showScores = showScores,
            upVotes = post.upvotes,
            downVotes = post.downvotes,
            upVoted = post.myVote > 0,
            downVoted = post.myVote < 0,
            saved = post.saved,
            onUpVote = onUpVote,
            onDownVote = onDownVote,
            onSave = onSave,
            onReply = onReply,
            publishDate = post.publishDate,
            updateDate = post.updateDate,
            optionsMenuOpen = optionsMenuOpen,
            options = options,
            onOptionSelected = onOptionSelected,
            actionButtonsActive = actionButtonsActive,
        )
    }
}

@Composable
private fun ExtendedPost(
    modifier: Modifier = Modifier,
    post: PostModel,
    isFromModerator: Boolean = false,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    showScores: Boolean = true,
    hideAuthor: Boolean = false,
    blurNsfw: Boolean = true,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    showBody: Boolean = false,
    limitBodyHeight: Boolean = false,
    fullHeightImage: Boolean = true,
    roundedCornerImage: Boolean = true,
    actionButtonsActive: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    options: List<Option> = emptyList(),
    onOpenCommunity: ((CommunityModel, String) -> Unit)? = null,
    onOpenCreator: ((UserModel, String) -> Unit)? = null,
    onOpenPost: ((PostModel, String) -> Unit)? = null,
    onOpenWeb: ((String) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOpenImage: ((String) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
) {
    val settingsRepository = remember { getSettingsRepository() }
    val settings by settingsRepository.currentSettings.collectAsState()
    val uriHandler = LocalUriHandler.current
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val optionsMenuOpen = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        optionsMenuOpen.value = true
                    },
                    onTap = {
                        onClick?.invoke()
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        CommunityAndCreatorInfo(
            modifier = Modifier.padding(horizontal = Spacing.xxs),
            community = post.community,
            creator = post.creator.takeIf { !hideAuthor },
            featured = post.featuredCommunity,
            featuredLocal = post.featuredLocal,
            locked = post.locked,
            isFromModerator = isFromModerator,
            onOpenCommunity = rememberCallbackArgs { community ->
                onOpenCommunity?.invoke(community, "")
            },
            onOpenCreator = rememberCallbackArgs { user ->
                onOpenCreator?.invoke(user, "")
            },
            autoLoadImages = autoLoadImages,
            preferNicknames = preferNicknames,
            onDoubleClick = onDoubleClick,
            onLongClick = rememberCallback {
                optionsMenuOpen.value = true
            },
        )
        CustomizedContent(ContentFontClass.Title) {
            PostCardTitle(
                modifier = Modifier.padding(
                    vertical = Spacing.xs,
                    horizontal = Spacing.xs,
                ),
                text = post.title,
                bolder = showBody,
                autoLoadImages = autoLoadImages,
                onOpenCommunity = onOpenCommunity,
                onOpenUser = onOpenCreator,
                onOpenPost = onOpenPost,
                onOpenWeb = onOpenWeb,
                onClick = onClick,
                onOpenImage = onOpenImage,
                onDoubleClick = onDoubleClick,
                onLongClick = {
                    optionsMenuOpen.value = true
                },
            )
        }

        if (post.videoUrl.isNotEmpty()) {
            PostCardVideo(
                modifier = Modifier.padding(vertical = Spacing.xxs),
                url = post.videoUrl,
                blurred = blurNsfw && post.nsfw,
                autoLoadImages = autoLoadImages,
                backgroundColor = backgroundColor,
                onOpen = onClick,
            )
        } else {
            PostCardImage(
                modifier = Modifier
                    .padding(vertical = Spacing.xxs)
                    .then(
                        if (roundedCornerImage) {
                            Modifier.clip(RoundedCornerShape(CornerSize.xl))
                        } else {
                            Modifier
                        }
                    ).then(
                        if (fullHeightImage) {
                            Modifier
                        } else {
                            Modifier.heightIn(max = 200.dp)
                        }
                    ),
                imageUrl = post.imageUrl,
                blurred = blurNsfw && post.nsfw,
                onImageClick = onOpenImage,
                onDoubleClick = onDoubleClick,
                autoLoadImages = autoLoadImages,
                onLongClick = {
                    optionsMenuOpen.value = true
                },
            )
        }

        if (showBody) {
            if (post.removed) {
                Text(
                    text = LocalXmlStrings.current.messageContentRemoved,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                )
            } else {
                CustomizedContent(ContentFontClass.Body) {
                    PostCardBody(
                        modifier = Modifier.padding(
                            top = Spacing.xxs,
                            start = Spacing.xs,
                            end = Spacing.xs,
                        ),
                        text = post.text,
                        maxLines = if (limitBodyHeight) {
                            settings.postBodyMaxLines
                        } else {
                            null
                        },
                        autoLoadImages = autoLoadImages,
                        onClick = onClick,
                        onOpenCommunity = onOpenCommunity,
                        onOpenUser = onOpenCreator,
                        onOpenPost = onOpenPost,
                        onOpenImage = onOpenImage,
                        onOpenWeb = onOpenWeb,
                        onDoubleClick = onDoubleClick,
                        onLongClick = {
                            optionsMenuOpen.value = true
                        },
                    )
                }
            }
        }
        if (post.url != post.imageUrl && post.url != post.videoUrl && !post.url.isNullOrEmpty()) {
            val url = post.url.orEmpty()
            PostLinkBanner(
                modifier = Modifier
                    .padding(vertical = Spacing.xs)
                    .onClick(
                        onClick = rememberCallback {
                            navigationCoordinator.handleUrl(
                                url = url,
                                openExternal = settings.openUrlsInExternalBrowser,
                                uriHandler = uriHandler,
                                onOpenWeb = onOpenWeb,
                                onOpenCommunity = onOpenCommunity,
                                onOpenPost = onOpenPost,
                                onOpenUser = onOpenCreator,
                            )
                        },
                        onDoubleClick = onDoubleClick ?: {},
                    ),
                url = url,
            )
        }
        PostCardFooter(
            modifier = Modifier.padding(top = Spacing.xs),
            comments = post.comments,
            voteFormat = voteFormat,
            score = post.score,
            showScores = showScores,
            upVotes = post.upvotes,
            downVotes = post.downvotes,
            upVoted = post.myVote > 0,
            downVoted = post.myVote < 0,
            saved = post.saved,
            onUpVote = onUpVote,
            onDownVote = onDownVote,
            onSave = onSave,
            onReply = onReply,
            publishDate = post.publishDate,
            updateDate = post.updateDate,
            options = options,
            optionsMenuOpen = optionsMenuOpen,
            onOptionSelected = onOptionSelected,
            actionButtonsActive = actionButtonsActive,
        )
    }
}
