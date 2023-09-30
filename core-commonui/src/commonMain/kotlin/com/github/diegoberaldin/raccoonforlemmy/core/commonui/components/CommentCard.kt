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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository

@Composable
fun CommentCard(
    comment: CommentModel,
    background: Color = MaterialTheme.colorScheme.background,
    modifier: Modifier = Modifier,
    hideAuthor: Boolean = false,
    options: List<String> = emptyList(),
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onOptionSelected: ((Int) -> Unit)? = null,
) {
    val themeRepository = remember { getThemeRepository() }
    val fontScale by themeRepository.contentFontScale.collectAsState()
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = fontScale,
        ),
    ) {
        Column(
            modifier = modifier.background(background)
        ) {
            var commentHeight by remember { mutableStateOf(0f) }
            val barWidth = 2.dp
            val barColor = themeRepository.getCommentBarColor(
                depth = comment.depth,
                maxDepth = CommentRepository.MAX_COMMENT_DEPTH,
                startColor = MaterialTheme.colorScheme.primary,
                endColor = MaterialTheme.colorScheme.background,
            )
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
                        creator = comment.creator.takeIf { !hideAuthor },
                        onOpenCreator = onOpenCreator,
                        onOpenCommunity = onOpenCommunity,
                    )
                    PostCardBody(
                        text = comment.text,
                    )
                    PostCardFooter(
                        score = comment.score,
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
                        .width(barWidth)
                        .height(commentHeight.toLocalDp())
                        .background(color = barColor)
                )
            }
            Box(
                modifier = Modifier
                    .height(Dp.Hairline)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}
