package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository

@Composable
fun CollapsedCommentCard(
    comment: CommentModel,
    modifier: Modifier = Modifier,
    separateUpAndDownVotes: Boolean = false,
    autoLoadImages: Boolean = true,
    options: List<Option> = emptyList(),
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onToggleExpanded: (() -> Unit)? = null,
) {
    val themeRepository = remember { getThemeRepository() }
    var commentHeight by remember { mutableStateOf(0f) }
    val barWidth = 2.dp
    val barColor = themeRepository.getCommentBarColor(
        depth = comment.depth,
        maxDepth = CommentRepository.MAX_COMMENT_DEPTH,
        startColor = MaterialTheme.colorScheme.primary,
        endColor = MaterialTheme.colorScheme.background,
    )
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(
                start = (10 * comment.depth).dp
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
                    autoLoadImages = autoLoadImages,
                    onToggleExpanded = {
                        onToggleExpanded?.invoke()
                    },
                    onOpenCreator = onOpenCreator,
                )
                PostCardFooter(
                    modifier = Modifier.padding(top = Spacing.xs),
                    score = comment.score,
                    separateUpAndDownVotes = separateUpAndDownVotes,
                    upvotes = comment.upvotes,
                    downvotes = comment.downvotes,
                    saved = comment.saved,
                    upVoted = comment.myVote > 0,
                    downVoted = comment.myVote < 0,
                    comments = comment.comments,
                    onUpVote = onUpVote,
                    onDownVote = onDownVote,
                    onSave = onSave,
                    onReply = onReply,
                    date = comment.publishDate,
                    options = options,
                    onOptionSelected = onOptionSelected,
                )
            }
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
