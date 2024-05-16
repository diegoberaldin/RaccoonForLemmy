package com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
internal fun ContentPreview(
    modifier: Modifier = Modifier,
    postLayout: PostLayout,
    preferNicknames: Boolean,
    showScores: Boolean,
    voteFormat: VoteFormat,
    fullHeightImage: Boolean,
    fullWidthImage: Boolean,
    commentBarThickness: Int,
    commentIndentAmount: Int,
) {
    Column {
        PostCard(
            modifier = modifier,
            post = ContentPreviewData.post,
            preferNicknames = preferNicknames,
            showScores = showScores,
            postLayout = postLayout,
            voteFormat = voteFormat,
            fullHeightImage = fullHeightImage,
            fullWidthImage = fullWidthImage,
            includeFullBody = true,
            limitBodyHeight = true,
        )

        if (postLayout != PostLayout.Card) {
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
        } else {
            Spacer(modifier = Modifier.height(Spacing.interItem))
        }

        CommentCard(
            comment = ContentPreviewData.comment1,
            voteFormat = voteFormat,
            preferNicknames = preferNicknames,
            showScores = showScores,
            indentAmount = commentIndentAmount,
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = Spacing.xxxs),
            thickness = 0.25.dp,
        )
        CommentCard(
            comment = ContentPreviewData.comment2,
            isOp = true,
            voteFormat = voteFormat,
            preferNicknames = preferNicknames,
            showScores = showScores,
            barThickness = commentBarThickness,
            indentAmount = commentIndentAmount,
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = Spacing.xxxs),
            thickness = 0.25.dp,
        )
        CommentCard(
            comment = ContentPreviewData.comment3,
            voteFormat = voteFormat,
            preferNicknames = preferNicknames,
            showScores = showScores,
            barThickness = commentBarThickness,
            indentAmount = commentIndentAmount,
        )
    }
}

private object ContentPreviewData {
    val post =
        PostModel(
            title = "Post title",
            text =
                """
Lorem ipsum **dolor** sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.

Ut enim ad minim veniam, quis *nostrud* exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat:
- duis aute irure dolor
- in reprehenderit in voluptate

velit esse cillum dolore eu fugiat nulla pariatur.
                """.trimIndent(),
            thumbnailUrl = "https://feddit.it/pictrs/image/4c6be082-0bf1-4095-b1cd-5fba8b602832.webp?format=webp&thumbnail=256",
            url = "https://github.com/diegoberaldin/RaccoonForLemmy",
            publishDate = "2024-01-01T12:00:00Z",
            upvotes = 2,
            downvotes = 1,
            score = 1,
            comments = 1,
            community =
                CommunityModel(
                    name = "somecommunity",
                    title = "Some Community",
                    host = "example.com",
                ),
            creator =
                UserModel(
                    name = "johndoe",
                    host = "example.com",
                    displayName = "John Doe",
                ),
        )

    val comment1 =
        CommentModel(
            text = "Excepteur sint occaecat cupidatat non proident.",
            publishDate = "2024-01-02T12:00:00Z",
            path = "0.1",
            creator =
                UserModel(
                    name = "marysmith",
                    host = "example.com",
                    displayName = "Mary Smith",
                ),
            upvotes = 2,
            downvotes = 1,
            score = 1,
            comments = 2,
        )

    val comment2 =
        CommentModel(
            text = "Sunt in culpa qui officia deserunt mollit anim id est laborum.",
            publishDate = "2024-01-03T12:00:00Z",
            path = "0.1.2",
            creator =
                UserModel(
                    name = "johndoe",
                    host = "example.com",
                    displayName = "John Doe",
                ),
            upvotes = 2,
            downvotes = 1,
            score = 1,
            comments = 1,
        )
    val comment3 =
        CommentModel(
            text = "Praesent sed congue leo, at hendrerit lorem.",
            publishDate = "2024-01-03T12:00:00Z",
            path = "0.1.2.3",
            creator =
                UserModel(
                    name = "marysmith",
                    host = "example.com",
                    displayName = "Mary Smith",
                ),
            upvotes = 1,
            downvotes = -2,
            score = -1,
            comments = 0,
        )
}
