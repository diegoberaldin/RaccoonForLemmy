package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.looksLikeAnImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.imageUrl

@Composable
fun PostCard(
    modifier: Modifier = Modifier,
    post: PostModel,
    autoLoadImages: Boolean = true,
    hideAuthor: Boolean = false,
    postLayout: PostLayout = PostLayout.Card,
    separateUpAndDownVotes: Boolean = false,
    includeFullBody: Boolean = false,
    fullHeightImage: Boolean = true,
    limitBodyHeight: Boolean = false,
    blurNsfw: Boolean = true,
    options: List<Option> = emptyList(),
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.let {
            if (postLayout == PostLayout.Card) {
                it.padding(horizontal = Spacing.xs).background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                    shape = RoundedCornerShape(CornerSize.l),
                ).padding(Spacing.s)
            } else {
                it
            }
        }.onClick(rememberCallback { onClick?.invoke() }),
    ) {
        if (postLayout != PostLayout.Compact) {
            ExtendedPost(
                post = post,
                hideAuthor = hideAuthor,
                backgroundColor = when (postLayout) {
                    PostLayout.Card -> MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
                    else -> MaterialTheme.colorScheme.background
                },
                showBody = includeFullBody || postLayout == PostLayout.Full,
                limitBodyHeight = limitBodyHeight,
                separateUpAndDownVotes = separateUpAndDownVotes,
                autoLoadImages = autoLoadImages,
                roundedCornerImage = postLayout == PostLayout.Card,
                fullHeightImage = fullHeightImage,
                blurNsfw = blurNsfw,
                options = options,
                onOpenCommunity = onOpenCommunity,
                onOpenCreator = onOpenCreator,
                onUpVote = onUpVote,
                onDownVote = onDownVote,
                onSave = onSave,
                onReply = onReply,
                onImageClick = onImageClick,
                onOptionSelected = onOptionSelected,
                onClick = onClick,
            )
        } else {
            CompactPost(
                post = post,
                hideAuthor = hideAuthor,
                blurNsfw = blurNsfw,
                separateUpAndDownVotes = separateUpAndDownVotes,
                autoLoadImages = autoLoadImages,
                options = options,
                onOpenCommunity = onOpenCommunity,
                onOpenCreator = onOpenCreator,
                onUpVote = onUpVote,
                onDownVote = onDownVote,
                onSave = onSave,
                onReply = onReply,
                onImageClick = onImageClick,
                onOptionSelected = onOptionSelected,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun CompactPost(
    modifier: Modifier = Modifier,
    post: PostModel,
    autoLoadImages: Boolean = true,
    hideAuthor: Boolean,
    blurNsfw: Boolean,
    separateUpAndDownVotes: Boolean,
    options: List<Option> = emptyList(),
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
    ) {
        CommunityAndCreatorInfo(
            community = post.community,
            creator = post.creator.takeIf { !hideAuthor },
            onOpenCommunity = onOpenCommunity,
            onOpenCreator = onOpenCreator,
            autoLoadImages = autoLoadImages,
        )
        Row(
            modifier = Modifier.padding(horizontal = Spacing.s),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            ScaledContent {
                PostCardTitle(
                    modifier = Modifier.weight(0.75f),
                    text = post.title,
                    autoLoadImages = autoLoadImages,
                    onClick = onClick,
                )
            }
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
                onImageClick = onImageClick,
            )
        }
        PostCardFooter(
            comments = post.comments,
            separateUpAndDownVotes = separateUpAndDownVotes,
            score = post.score,
            upvotes = post.upvotes,
            downvotes = post.downvotes,
            upVoted = post.myVote > 0,
            downVoted = post.myVote < 0,
            saved = post.saved,
            onUpVote = onUpVote,
            onDownVote = onDownVote,
            onSave = onSave,
            onReply = onReply,
            date = post.publishDate,
            options = options,
            onOptionSelected = onOptionSelected,
        )
    }
}

@Composable
private fun ExtendedPost(
    modifier: Modifier = Modifier,
    post: PostModel,
    autoLoadImages: Boolean = true,
    hideAuthor: Boolean = false,
    blurNsfw: Boolean = true,
    separateUpAndDownVotes: Boolean = false,
    showBody: Boolean = false,
    limitBodyHeight: Boolean = false,
    fullHeightImage: Boolean = true,
    roundedCornerImage: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    options: List<Option> = emptyList(),
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.background(backgroundColor),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        CommunityAndCreatorInfo(
            modifier = Modifier.padding(horizontal = Spacing.xxs),
            community = post.community,
            creator = post.creator.takeIf { !hideAuthor },
            onOpenCommunity = onOpenCommunity,
            onOpenCreator = onOpenCreator,
            autoLoadImages = autoLoadImages,
        )
        ScaledContent {
            PostCardTitle(
                modifier = Modifier.padding(
                    vertical = Spacing.xs,
                    horizontal = Spacing.xs,
                ),
                text = post.title,
                autoLoadImages = autoLoadImages,
                onClick = onClick,
            )
        }

        PostCardImage(
            modifier = Modifier.let {
                if (roundedCornerImage) {
                    it.clip(RoundedCornerShape(CornerSize.xl))
                } else {
                    it
                }
            }.let {
                if (fullHeightImage) {
                    it
                } else {
                    it.heightIn(max = 200.dp)
                }
            },
            imageUrl = post.imageUrl,
            blurred = blurNsfw && post.nsfw,
            onImageClick = onImageClick,
            autoLoadImages = autoLoadImages,
        )
        if (showBody) {
            ScaledContent {
                Box {
                    val maxHeight = 200.dp
                    val maxHeightPx = maxHeight.toLocalPixel()
                    var textHeightPx by remember { mutableStateOf(0f) }
                    PostCardBody(
                        modifier = Modifier.let {
                            if (limitBodyHeight) {
                                it.heightIn(max = maxHeight)
                            } else {
                                it
                            }
                        }.padding(horizontal = Spacing.xs).onGloballyPositioned {
                            textHeightPx = it.size.toSize().height
                        },
                        text = post.text,
                        autoLoadImages = autoLoadImages,
                        onClick = onClick,
                    )
                    if (limitBodyHeight && textHeightPx >= maxHeightPx) {
                        Box(
                            modifier = Modifier.height(Spacing.xxl).fillMaxWidth()
                                .align(Alignment.BottomCenter).background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            backgroundColor,
                                        ),
                                    ),
                                ),
                        )
                    }
                }
            }
        }
        if (post.url != post.imageUrl) {
            PostLinkBanner(
                modifier = Modifier.padding(vertical = Spacing.xs),
                url = post.url?.takeIf { !it.looksLikeAnImage }.orEmpty(),
            )
        }
        PostCardFooter(
            modifier = Modifier.padding(top = Spacing.xs),
            comments = post.comments,
            separateUpAndDownVotes = separateUpAndDownVotes,
            score = post.score,
            upvotes = post.upvotes,
            downvotes = post.downvotes,
            upVoted = post.myVote > 0,
            downVoted = post.myVote < 0,
            saved = post.saved,
            onUpVote = onUpVote,
            onDownVote = onDownVote,
            onSave = onSave,
            onReply = onReply,
            date = post.publishDate,
            options = options,
            onOptionSelected = onOptionSelected,
        )
    }
}
