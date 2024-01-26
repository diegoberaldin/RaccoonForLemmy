package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface InboxCardType {
    data object Mention : InboxCardType
    data object Reply : InboxCardType
}

@Composable
fun InboxCard(
    mention: PersonMentionModel,
    type: InboxCardType,
    autoLoadImages: Boolean = true,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    postLayout: PostLayout = PostLayout.Card,
    options: List<Option> = emptyList(),
    onImageClick: (String) -> Unit,
    onOpenPost: (PostModel) -> Unit,
    onOpenCreator: (UserModel) -> Unit,
    onOpenCommunity: (CommunityModel) -> Unit,
    onUpVote: ((CommentModel) -> Unit)? = null,
    onDownVote: ((CommentModel) -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    Box(
        modifier = Modifier.then(
            if (postLayout == PostLayout.Card) {
                Modifier
                    .padding(horizontal = Spacing.xs)
                    .shadow(elevation = 5.dp, shape = RoundedCornerShape(CornerSize.l))
                    .clip(RoundedCornerShape(CornerSize.l))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                        shape = RoundedCornerShape(CornerSize.l),
                    )
                    .padding(Spacing.s)
            } else {
                Modifier.background(MaterialTheme.colorScheme.background)
            }
        ).onClick(
            onClick = rememberCallback {
                onOpenPost(mention.post)
            },
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            InboxCardHeader(
                mention = mention,
                type = type,
            )
            if (mention.comment.removed) {
                Text(
                    text = stringResource(MR.strings.message_content_removed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                )
            } else {
                CustomizedContent {
                    PostCardBody(
                        modifier = Modifier.padding(
                            horizontal = Spacing.xs,
                        ),
                        text = mention.comment.text.substringBefore("\n"),
                        autoLoadImages = autoLoadImages,
                        onOpenImage = onImageClick,
                        onClick = {
                            onOpenPost(mention.post)
                        }
                    )
                }
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
                upVotes = mention.upvotes,
                downVotes = mention.downvotes,
                voteFormat = voteFormat,
                upVoted = mention.myVote > 0,
                downVoted = mention.myVote < 0,
                options = options,
                onOpenCommunity = onOpenCommunity,
                onOpenCreator = rememberCallbackArgs { user ->
                    onOpenCreator(user)
                },
                onUpVote = rememberCallback {
                    onUpVote?.invoke(mention.comment)
                },
                onDownVote = rememberCallback {
                    onDownVote?.invoke(mention.comment)
                },
                onOptionSelected = onOptionSelected,
            )
        }
    }
}
