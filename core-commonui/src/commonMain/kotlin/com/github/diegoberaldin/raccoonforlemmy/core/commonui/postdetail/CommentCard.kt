package com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardFooter
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardSubtitle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel

@Composable
fun CommentCard(
    comment: CommentModel,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth().background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(CornerSize.m),
        ).padding(
            vertical = Spacing.lHalf,
            horizontal = Spacing.s,
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            PostCardSubtitle(
                creator = comment.creator,
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
            )
        }
    }
}
