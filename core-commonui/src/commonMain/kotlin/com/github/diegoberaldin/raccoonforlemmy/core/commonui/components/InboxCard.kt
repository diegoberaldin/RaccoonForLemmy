package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

sealed interface InboxCardType {
    data object Mention : InboxCardType
    data object Reply : InboxCardType
}

@Composable
fun InboxCard(
    mention: PersonMentionModel,
    type: InboxCardType,
    autoLoadImages: Boolean = true,
    separateUpAndDownVotes: Boolean = true,
    postLayout: PostLayout = PostLayout.Card,
    onOpenPost: (PostModel) -> Unit,
    onOpenCreator: (UserModel) -> Unit,
    onOpenCommunity: (CommunityModel) -> Unit,
    onUpVote: ((CommentModel) -> Unit)? = null,
    onDownVote: ((CommentModel) -> Unit)? = null,
) {
    Box(
        modifier = Modifier.let {
            if (postLayout == PostLayout.Card) {
                it.padding(horizontal = Spacing.xs)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                        shape = RoundedCornerShape(CornerSize.l),
                    ).padding(Spacing.s)
            } else {
                it.background(MaterialTheme.colorScheme.background)
            }
        }.onClick {
            onOpenPost(mention.post)
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            InboxCardHeader(
                mention = mention,
                type = type,
            )
            ScaledContent {
                PostCardBody(
                    modifier = Modifier.padding(
                        horizontal = Spacing.xs,
                    ),
                    text = mention.comment.text,
                    autoLoadImages = autoLoadImages,
                    onClick = {
                        onOpenPost(mention.post)
                    }
                )
            }
            InboxReplySubtitle(
                modifier = Modifier.padding(
                    start = Spacing.xs,
                    end = Spacing.xs,
                    top = Spacing.xs,
                ),
                creator = mention.creator,
                community = mention.community,
                autoLoadImages = autoLoadImages,
                date = mention.publishDate,
                score = mention.score,
                upvotes = mention.upvotes,
                downvotes = mention.downvotes,
                separateUpAndDownVotes = separateUpAndDownVotes,
                upVoted = mention.myVote > 0,
                downVoted = mention.myVote < 0,
                onOpenCommunity = onOpenCommunity,
                onOpenCreator = { user ->
                    onOpenCreator(user)
                },
                onUpVote = {
                    onUpVote?.invoke(mention.comment)
                },
                onDownVote = {
                    onDownVote?.invoke(mention.comment)
                },
            )
        }
    }
}
