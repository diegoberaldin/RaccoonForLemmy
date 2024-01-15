package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

private val barWidth = 1.25.dp
private const val INDENT_AMOUNT = 3

@Composable
fun CommentCard(
    comment: CommentModel,
    modifier: Modifier = Modifier,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    hideAuthor: Boolean = false,
    hideCommunity: Boolean = true,
    hideIndent: Boolean = false,
    autoLoadImages: Boolean = true,
    actionButtonsActive: Boolean = true,
    isOp: Boolean = false,
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
    val barColor = themeRepository.getCommentBarColor(
        depth = comment.depth,
        commentBarTheme = commentBarTheme,
    )

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.onClick(
                onClick = onClick ?: {},
                onDoubleClick = onDoubleClick ?: {}
            ).padding(
                start = if (hideIndent) 0.dp else (INDENT_AMOUNT * comment.depth).dp
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = barWidth)
                    .fillMaxWidth()
                    .padding(
                        vertical = Spacing.xxs,
                        horizontal = Spacing.s,
                    ).onGloballyPositioned {
                        commentHeight = it.size.toSize().height
                    }
            ) {
                CommunityAndCreatorInfo(
                    modifier = Modifier.padding(top = Spacing.xxs),
                    iconSize = IconSize.s,
                    autoLoadImages = autoLoadImages,
                    creator = comment.creator.takeIf { !hideAuthor },
                    community = comment.community.takeIf { !hideCommunity },
                    indicatorExpanded = comment.expanded,
                    distinguished = comment.distinguished,
                    isOp = isOp,
                    onOpenCreator = rememberCallbackArgs { user ->
                        onOpenCreator?.invoke(user, "")
                    },
                    onOpenCommunity = rememberCallbackArgs { community ->
                        onOpenCommunity?.invoke(community, "")
                    },
                    onToggleExpanded = onToggleExpanded,
                )
                if (comment.removed) {
                    Text(
                        text = stringResource(MR.strings.message_content_removed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    )
                } else {
                    CustomizedContent {
                        PostCardBody(
                            text = comment.text,
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
                PostCardFooter(
                    modifier = Modifier.padding(top = Spacing.xs),
                    score = comment.score,
                    voteFormat = voteFormat,
                    upVotes = comment.upvotes,
                    downVotes = comment.downvotes,
                    saved = comment.saved,
                    upVoted = comment.myVote > 0,
                    downVoted = comment.myVote < 0,
                    comments = comment.comments,
                    actionButtonsActive = actionButtonsActive,
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
            if (!hideIndent && comment.depth > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.xxs)
                        .width(barWidth)
                        .height(commentHeight.toLocalDp())
                        .background(color = barColor)
                )
            }
        }
    }
}
