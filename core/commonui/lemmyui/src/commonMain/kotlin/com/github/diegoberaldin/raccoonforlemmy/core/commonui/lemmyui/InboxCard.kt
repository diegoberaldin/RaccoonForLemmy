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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
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
    preferNicknames: Boolean = true,
    showScores: Boolean = true,
    voteFormat: VoteFormat = VoteFormat.Aggregated,
    postLayout: PostLayout = PostLayout.Card,
    options: List<Option> = emptyList(),
    onImageClick: (String) -> Unit,
    onOpenPost: (PostModel) -> Unit,
    onOpenCreator: (UserModel) -> Unit,
    onOpenCommunity: (CommunityModel) -> Unit,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
    onReply: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.then(
            if (postLayout == PostLayout.Card) {
                Modifier
                    .padding(horizontal = Spacing.xs)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(CornerSize.l),
                    )
                    .clip(RoundedCornerShape(CornerSize.l))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                    )
                    .padding(vertical = Spacing.s,)
            } else {
                Modifier.background(MaterialTheme.colorScheme.background)
            }
        ).onClick(
            onClick = {
                onOpenPost(mention.post)
            },
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            InboxCardHeader(
                modifier = Modifier.padding(horizontal = Spacing.s),
                mention = mention,
                type = type,
            )
            if (mention.comment.removed) {
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
                            horizontal = Spacing.s,
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
                    start = Spacing.s,
                    end = Spacing.s,
                    top = Spacing.s,
                ),
                creator = mention.creator,
                community = mention.community,
                autoLoadImages = autoLoadImages,
                preferNicknames = preferNicknames,
                date = mention.publishDate,
                score = mention.score,
                showScores = showScores,
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
                onUpVote = onUpVote,
                onDownVote = onDownVote,
                onOptionSelected = onOptionSelected,
                onReply = onReply,
            )
        }
    }
}
