package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
fun PostCard(
    modifier: Modifier = Modifier,
    post: PostModel,
    blurNsfw: Boolean,
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
) {
    val themeRepository = remember { getThemeRepository() }
    val fontScale by themeRepository.contentFontScale.collectAsState()
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = fontScale,
        ),
    ) {
        Card(
            modifier = modifier.background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(CornerSize.m),
            ).padding(
                vertical = Spacing.xxs,
                horizontal = Spacing.s,
            ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                PostCardTitle(
                    modifier = Modifier.padding(top = Spacing.s),
                    post = post
                )
                PostCardSubtitle(
                    community = post.community,
                    creator = post.creator?.copy(avatar = null),
                    onOpenCommunity = onOpenCommunity,
                    onOpenCreator = onOpenCreator,
                )
                PostCardImage(
                    post = post,
                    blurNsfw = blurNsfw,
                )
                Box {
                    PostCardBody(
                        modifier = Modifier.heightIn(max = 200.dp).padding(bottom = Spacing.xs),
                        text = post.text,
                    )
                    Box(
                        modifier = Modifier.height(Spacing.s).fillMaxWidth()
                            .align(Alignment.BottomCenter).background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                ),
                            ),
                    )
                }
                PostCardFooter(
                    comments = post.comments,
                    score = post.score,
                    upVoted = post.myVote > 0,
                    downVoted = post.myVote < 0,
                    saved = post.saved,
                    onUpVote = onUpVote,
                    onDownVote = onDownVote,
                    onSave = onSave,
                    onReply = onReply,
                    date = post.publishDate,
                )
            }
        }
    }
}
