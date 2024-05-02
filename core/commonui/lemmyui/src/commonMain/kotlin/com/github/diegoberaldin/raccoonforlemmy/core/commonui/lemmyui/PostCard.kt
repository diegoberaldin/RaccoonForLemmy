package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.looksLikeAVideo
import com.github.diegoberaldin.raccoonforlemmy.core.utils.looksLikeAnImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.getCustomTabsHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.toUrlOpeningMode
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.videoUrl

@Composable
fun PostCard(
    post: PostModel,
    modifier: Modifier = Modifier,
    isFromModerator: Boolean = false,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    showScores: Boolean = true,
    hideAuthor: Boolean = false,
    postLayout: PostLayout = PostLayout.Card,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    includeFullBody: Boolean = false,
    fullHeightImage: Boolean = true,
    fullWidthImage: Boolean = false,
    limitBodyHeight: Boolean = false,
    blurNsfw: Boolean = true,
    fadeRead: Boolean = false,
    showUnreadComments: Boolean = false,
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
    val markRead = post.read && fadeRead
    Box(
        modifier = modifier.then(
            if (postLayout == PostLayout.Card) {
                Modifier
                    .padding(horizontal = Spacing.xs)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(CornerSize.l)
                    )
                    .clip(RoundedCornerShape(CornerSize.l))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                    )
                    .padding(vertical = Spacing.s)
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
                fullWidthImage = fullWidthImage,
                blurNsfw = blurNsfw,
                markRead = markRead,
                actionButtonsActive = actionButtonsActive,
                showUnreadComments = showUnreadComments,
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
                markRead = markRead,
                voteFormat = voteFormat,
                autoLoadImages = autoLoadImages,
                fullHeightImage = fullHeightImage,
                preferNicknames = preferNicknames,
                showScores = showScores,
                actionButtonsActive = actionButtonsActive,
                showUnreadComments = showUnreadComments,
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
    isFromModerator: Boolean,
    autoLoadImages: Boolean,
    fullHeightImage: Boolean,
    preferNicknames: Boolean,
    showScores: Boolean,
    hideAuthor: Boolean,
    blurNsfw: Boolean,
    markRead: Boolean,
    actionButtonsActive: Boolean,
    voteFormat: VoteFormat,
    showUnreadComments: Boolean,
    options: List<Option>,
    onOpenCommunity: ((CommunityModel, String) -> Unit)?,
    onOpenCreator: ((UserModel, String) -> Unit)?,
    onOpenPost: ((PostModel, String) -> Unit)?,
    onOpenWeb: ((String) -> Unit)?,
    onUpVote: (() -> Unit)?,
    onDownVote: (() -> Unit)?,
    onSave: (() -> Unit)?,
    onReply: (() -> Unit)?,
    onOpenImage: ((String) -> Unit)?,
    onOptionSelected: ((OptionId) -> Unit)?,
    onClick: (() -> Unit)?,
    onDoubleClick: (() -> Unit)?,
) {
    val optionsMenuOpen = remember { mutableStateOf(false) }
    val settingsRepository = remember { getSettingsRepository() }
    val settings by settingsRepository.currentSettings.collectAsState()
    val uriHandler = LocalUriHandler.current
    val customTabsHelper = remember { getCustomTabsHelper() }
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val postLinkUrl =
        post.url.orEmpty().takeIf { !it.looksLikeAnImage && !it.looksLikeAVideo }.orEmpty()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.xs)
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
            modifier = Modifier.padding(horizontal = Spacing.xs),
            community = post.community,
            creator = post.creator.takeIf { !hideAuthor },
            featuredCommunity = post.featuredCommunity,
            featuredLocal = post.featuredLocal,
            locked = post.locked,
            markRead = markRead,
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
            modifier = Modifier.padding(horizontal = Spacing.xs),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            CustomizedContent(ContentFontClass.Title) {
                PostCardTitle(
                    modifier = Modifier.weight(0.75f),
                    text = post.title,
                    autoLoadImages = autoLoadImages,
                    markRead = markRead,
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
                    modifier = Modifier
                        .weight(0.25f)
                        .padding(vertical = Spacing.xxs),
                    url = post.videoUrl,
                    blurred = blurNsfw && post.nsfw,
                    autoLoadImages = autoLoadImages,
                    onOpen = rememberCallback {
                        if (postLinkUrl.isNotEmpty()) {
                            navigationCoordinator.handleUrl(
                                url = postLinkUrl,
                                openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                                uriHandler = uriHandler,
                                customTabsHelper = customTabsHelper,
                                onOpenWeb = onOpenWeb,
                                onOpenCommunity = onOpenCommunity,
                                onOpenPost = onOpenPost,
                                onOpenUser = onOpenCreator,
                            )
                        } else {
                            onClick?.invoke()
                        }
                    },
                )
            } else {
                PostCardImage(
                    modifier = Modifier
                        .weight(0.25f)
                        .then(
                            if (fullHeightImage) {
                                Modifier
                            } else {
                                Modifier.aspectRatio(1f)
                            }
                        )
                        .padding(vertical = Spacing.xs)
                        .clip(RoundedCornerShape(CornerSize.s)),
                    minHeight = Dp.Unspecified,
                    maxHeight = Dp.Unspecified,
                    imageUrl = post.imageUrl,
                    autoLoadImages = autoLoadImages,
                    loadButtonContent = @Composable {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    },
                    blurred = blurNsfw && post.nsfw,
                    onImageClick = rememberCallbackArgs { url ->
                        if (postLinkUrl.isNotEmpty()) {
                            navigationCoordinator.handleUrl(
                                url = postLinkUrl,
                                openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                                uriHandler = uriHandler,
                                customTabsHelper = customTabsHelper,
                                onOpenWeb = onOpenWeb,
                                onOpenCommunity = onOpenCommunity,
                                onOpenPost = onOpenPost,
                                onOpenUser = onOpenCreator,
                            )
                        } else {
                            onOpenImage?.invoke(url)
                        }
                    },
                    onDoubleClick = onDoubleClick,
                    onLongClick = rememberCallback {
                        optionsMenuOpen.value = true
                    },
                )
            }
        }
        PostCardFooter(
            modifier = Modifier.padding(
                top = Spacing.xxs,
                start = Spacing.xs,
                end = Spacing.xs,
            ),
            markRead = markRead,
            comments = post.comments,
            voteFormat = voteFormat,
            score = post.score,
            showScores = showScores,
            unreadComments = post.unreadComments.takeIf {
                it != null && it > 0 && showUnreadComments && it != post.comments
            },
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
    isFromModerator: Boolean,
    autoLoadImages: Boolean,
    preferNicknames: Boolean,
    showScores: Boolean,
    hideAuthor: Boolean,
    blurNsfw: Boolean,
    markRead: Boolean,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    showBody: Boolean,
    limitBodyHeight: Boolean,
    fullHeightImage: Boolean,
    fullWidthImage: Boolean = false,
    roundedCornerImage: Boolean,
    actionButtonsActive: Boolean,
    backgroundColor: Color,
    showUnreadComments: Boolean,
    options: List<Option>,
    onOpenCommunity: ((CommunityModel, String) -> Unit)?,
    onOpenCreator: ((UserModel, String) -> Unit)?,
    onOpenPost: ((PostModel, String) -> Unit)?,
    onOpenWeb: ((String) -> Unit)?,
    onUpVote: (() -> Unit)?,
    onDownVote: (() -> Unit)?,
    onSave: (() -> Unit)?,
    onReply: (() -> Unit)?,
    onOpenImage: ((String) -> Unit)?,
    onOptionSelected: ((OptionId) -> Unit)?,
    onClick: (() -> Unit)?,
    onDoubleClick: (() -> Unit)?,
) {
    val settingsRepository = remember { getSettingsRepository() }
    val settings by settingsRepository.currentSettings.collectAsState()
    val uriHandler = LocalUriHandler.current
    val customTabsHelper = remember { getCustomTabsHelper() }
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val optionsMenuOpen = remember { mutableStateOf(false) }
    val postLinkUrl = post.url.orEmpty().takeIf {
        it != post.imageUrl && it != post.videoUrl && !it.looksLikeAnImage && !it.looksLikeAVideo
    }.orEmpty()

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
            modifier = Modifier.padding(horizontal = Spacing.s),
            community = post.community,
            creator = post.creator.takeIf { !hideAuthor },
            featuredCommunity = post.featuredCommunity,
            featuredLocal = post.featuredLocal,
            locked = post.locked,
            markRead = markRead,
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
                    horizontal = Spacing.s,
                ),
                text = post.title,
                markRead = markRead,
                bolder = showBody,
                autoLoadImages = autoLoadImages,
                onOpenCommunity = onOpenCommunity,
                onOpenUser = onOpenCreator,
                onOpenPost = onOpenPost,
                onOpenWeb = onOpenWeb,
                onClick = onClick,
                onOpenImage = onOpenImage,
                onDoubleClick = onDoubleClick,
                onLongClick = rememberCallback {
                    optionsMenuOpen.value = true
                },
            )
        }

        if (post.videoUrl.isNotEmpty()) {
            PostCardVideo(
                modifier = Modifier
                    .padding(
                        vertical = Spacing.xxs,
                        horizontal = if (fullWidthImage) 0.dp else Spacing.s,
                    ),
                url = post.videoUrl,
                blurred = blurNsfw && post.nsfw,
                autoLoadImages = autoLoadImages,
                backgroundColor = backgroundColor,
                onOpen = {
                    if (postLinkUrl.isNotEmpty()) {
                        navigationCoordinator.handleUrl(
                            url = postLinkUrl,
                            openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                            uriHandler = uriHandler,
                            customTabsHelper = customTabsHelper,
                            onOpenWeb = onOpenWeb,
                            onOpenCommunity = onOpenCommunity,
                            onOpenPost = onOpenPost,
                            onOpenUser = onOpenCreator,
                        )
                    } else {
                        onClick?.invoke()
                    }
                },
            )
        } else {
            PostCardImage(
                modifier = Modifier
                    .padding(
                        vertical = Spacing.xs,
                        horizontal = if (fullWidthImage) 0.dp else Spacing.s,
                    )
                    .then(
                        if (roundedCornerImage && !fullWidthImage) {
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
                onImageClick = rememberCallbackArgs { url ->
                    if (postLinkUrl.isNotEmpty()) {
                        navigationCoordinator.handleUrl(
                            url = postLinkUrl,
                            openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                            uriHandler = uriHandler,
                            customTabsHelper = customTabsHelper,
                            onOpenWeb = onOpenWeb,
                            onOpenCommunity = onOpenCommunity,
                            onOpenPost = onOpenPost,
                            onOpenUser = onOpenCreator,
                        )
                    } else {
                        onOpenImage?.invoke(url)
                    }
                },
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
                    modifier = Modifier.padding(horizontal = Spacing.s),
                    text = LocalXmlStrings.current.messageContentRemoved,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha),
                )
            } else {
                CustomizedContent(ContentFontClass.Body) {
                    PostCardBody(
                        modifier = Modifier.padding(
                            top = Spacing.xxs,
                            start = Spacing.s,
                            end = Spacing.s,
                        ),
                        text = post.text,
                        maxLines = if (limitBodyHeight) {
                            settings.postBodyMaxLines
                        } else {
                            null
                        },
                        autoLoadImages = autoLoadImages,
                        markRead = markRead,
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
        if (postLinkUrl.isNotEmpty() && postLinkUrl != post.imageUrl && postLinkUrl != post.videoUrl) {
            PostLinkBanner(
                modifier = Modifier
                    .padding(
                        top = Spacing.s,
                        bottom = Spacing.xxs,
                        start = Spacing.s,
                        end = Spacing.s,
                    )
                    .onClick(
                        onClick = rememberCallback {
                            navigationCoordinator.handleUrl(
                                url = postLinkUrl,
                                openingMode = settings.urlOpeningMode.toUrlOpeningMode(),
                                uriHandler = uriHandler,
                                customTabsHelper = customTabsHelper,
                                onOpenWeb = onOpenWeb,
                                onOpenCommunity = onOpenCommunity,
                                onOpenPost = onOpenPost,
                                onOpenUser = onOpenCreator,
                            )
                        },
                        onDoubleClick = onDoubleClick ?: {},
                    ),
                url = postLinkUrl,
            )
        }
        PostCardFooter(
            modifier = Modifier.padding(
                top = Spacing.xs,
                start = Spacing.s,
                end = Spacing.s,
            ),
            markRead = markRead,
            comments = post.comments,
            voteFormat = voteFormat,
            score = post.score,
            showScores = showScores,
            unreadComments = post.unreadComments.takeIf {
                it != null && it > 0 && showUnreadComments && it != post.comments
            },
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
