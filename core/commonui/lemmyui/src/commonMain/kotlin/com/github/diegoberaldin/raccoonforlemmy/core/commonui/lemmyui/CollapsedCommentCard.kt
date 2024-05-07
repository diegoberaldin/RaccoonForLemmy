package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

private const val INDENT_AMOUNT = 3

@Composable
fun CollapsedCommentCard(
    comment: CommentModel,
    modifier: Modifier = Modifier,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
    showScores: Boolean = true,
    actionButtonsActive: Boolean = true,
    isOp: Boolean = false,
    showBot: Boolean = false,
    options: List<Option> = emptyList(),
    onClick: (() -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onToggleExpanded: (() -> Unit)? = null,
) {
    val themeRepository = remember { getThemeRepository() }
    val commentBarTheme by themeRepository.commentBarTheme.collectAsState()
    var commentHeight by remember { mutableStateOf(0f) }
    val barWidth = 2.dp
    val barColor = themeRepository.getCommentBarColor(
        depth = comment.depth,
        commentBarTheme = commentBarTheme,
    )
    Column(
        modifier = modifier.onClick(
            onClick = onClick ?: {},
        )
    ) {
        Box(
            modifier = Modifier.padding(
                start = (INDENT_AMOUNT * comment.depth).dp
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
                    iconSize = IconSize.s,
                    creator = comment.creator,
                    indicatorExpanded = comment.expanded,
                    distinguished = comment.distinguished,
                    isOp = isOp,
                    isBot = comment.creator?.bot?.takeIf { showBot } ?: false,
                    autoLoadImages = autoLoadImages,
                    preferNicknames = preferNicknames,
                    onToggleExpanded = {
                        onToggleExpanded?.invoke()
                    },
                    onOpenCreator = onOpenCreator,
                )
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
                    onUpVote = onUpVote,
                    onDownVote = onDownVote,
                    onSave = onSave,
                    onReply = onReply,
                    publishDate = comment.publishDate,
                    updateDate = comment.updateDate,
                    actionButtonsActive = actionButtonsActive,
                    options = options,
                    onOptionSelected = onOptionSelected,
                )
            }
            if (comment.depth > 0) {
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
