package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

private val BAR_BASE_WIDTH_UNIT = 1.25.dp
private const val COMMENT_TEXT_SCALE_FACTOR = 0.97f

@Composable
fun CommentCard(
    comment: CommentModel,
    modifier: Modifier = Modifier,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    hideAuthor: Boolean = false,
    hideCommunity: Boolean = true,
    indentAmount: Int = 2,
    barThickness: Int = 1,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    showScores: Boolean = true,
    showExpandedIndicator: Boolean = true,
    actionButtonsActive: Boolean = true,
    isOp: Boolean = false,
    isMod: Boolean = false,
    showBot: Boolean = false,
    downVoteEnabled: Boolean = true,
    options: List<Option> = emptyList(),
    onClick: (() -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOpenCommunity: ((CommunityModel, String) -> Unit)? = null,
    onOpenCreator: ((UserModel, String) -> Unit)? = null,
    onOpenPost: ((PostModel, String) -> Unit)? = null,
    onOpenWeb: ((String) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onToggleExpanded: (() -> Unit)? = null,
) {
    val themeRepository = remember { getThemeRepository() }
    var commentHeight by remember { mutableStateOf(0f) }
    val commentBarTheme by themeRepository.commentBarTheme.collectAsState()
    val barColor =
        themeRepository.getCommentBarColor(
            depth = comment.depth,
            commentBarTheme = commentBarTheme,
        )
    val barWidth =
        if (comment.depth > 0) {
            BAR_BASE_WIDTH_UNIT * barThickness
        } else {
            0.dp
        }

    Column(
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .onClick(
                        onClick = onClick ?: {},
                        onDoubleClick = onDoubleClick ?: {},
                    ).padding(
                        start =
                            indentAmount
                                .takeIf {
                                    it > 0 && comment.depth > 0
                                }?.let {
                                    (it * comment.depth).dp + Spacing.xxxs
                                } ?: 0.dp,
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(start = barWidth)
                        .fillMaxWidth()
                        .padding(
                            vertical = Spacing.xxs,
                            horizontal = Spacing.s,
                        ).onGloballyPositioned {
                            commentHeight = it.size.toSize().height
                        },
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                CommunityAndCreatorInfo(
                    modifier = Modifier.padding(top = Spacing.xxs),
                    iconSize = IconSize.s,
                    autoLoadImages = autoLoadImages,
                    preferNicknames = preferNicknames,
                    creator = comment.creator.takeIf { !hideAuthor },
                    community = comment.community.takeIf { !hideCommunity },
                    indicatorExpanded = comment.expanded.takeIf { showExpandedIndicator },
                    distinguished = comment.distinguished,
                    isOp = isOp,
                    isMod = isMod,
                    isBot = comment.creator?.bot.takeIf { showBot } ?: false,
                    onOpenCreator =
                        rememberCallbackArgs { user ->
                            onOpenCreator?.invoke(user, "")
                        },
                    onOpenCommunity =
                        rememberCallbackArgs { community ->
                            onOpenCommunity?.invoke(community, "")
                        },
                    onToggleExpanded = onToggleExpanded,
                )
                if (comment.removed) {
                    Text(
                        text = LocalStrings.current.messageContentRemoved,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha),
                    )
                } else if (comment.deleted) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = LocalStrings.current.messageContentDeleted,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha),
                    )
                } else {
                    CustomizedContent(ContentFontClass.Body) {
                        CompositionLocalProvider(
                            LocalDensity provides
                                Density(
                                    density = LocalDensity.current.density,
                                    // additional downscale for font in comments
                                    fontScale = LocalDensity.current.fontScale * COMMENT_TEXT_SCALE_FACTOR,
                                ),
                        ) {
                            PostCardBody(
                                text = comment.text.orEmpty(),
                                autoLoadImages = autoLoadImages,
                                onClick = onClick,
                                onOpenImage = onImageClick,
                                onDoubleClick = onDoubleClick,
                                onOpenCommunity = onOpenCommunity,
                                onOpenUser = onOpenCreator,
                                onOpenPost = onOpenPost,
                                onOpenWeb = onOpenWeb,
                            )
                        }
                    }
                }
                PostCardFooter(
                    modifier = Modifier.padding(vertical = Spacing.xs),
                    score = comment.score,
                    showScores = showScores,
                    voteFormat = voteFormat,
                    upVotes = comment.upvotes,
                    downVotes = comment.downvotes,
                    saved = comment.saved,
                    upVoted = comment.myVote > 0,
                    downVoted = comment.myVote < 0,
                    comments = comment.comments,
                    actionButtonsActive = actionButtonsActive,
                    downVoteEnabled = downVoteEnabled,
                    onUpVote = onUpVote,
                    onDownVote = onDownVote,
                    onSave = onSave,
                    onReply = onReply,
                    publishDate = comment.publishDate,
                    updateDate = comment.updateDate,
                    options = options,
                    onOptionSelected = onOptionSelected,
                )
            }
            if (indentAmount > 0 && comment.depth > 0) {
                Box(
                    modifier =
                        Modifier
                            .padding(top = Spacing.xxs)
                            .width(barWidth)
                            .height(commentHeight.toLocalDp())
                            .background(color = barColor, shape = RoundedCornerShape(barWidth / 2)),
                )
            }
        }
    }
}
