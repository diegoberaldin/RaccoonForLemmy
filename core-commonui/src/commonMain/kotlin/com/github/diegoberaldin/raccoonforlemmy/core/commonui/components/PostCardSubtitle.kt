package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun PostCardSubtitle(
    modifier: Modifier = Modifier,
    community: CommunityModel? = null,
    creator: UserModel? = null,
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
) {
    val communityName = community?.name.orEmpty()
    val communityIcon = community?.icon.orEmpty()
    val communityHost = community?.host.orEmpty()
    val creatorName = creator?.name.orEmpty()
    val creatorAvatar = creator?.avatar.orEmpty()
    val creatorHost = creator?.host.orEmpty()
    val iconSize = 16.dp
    if (communityName.isNotEmpty() || creatorName.isNotEmpty()) {
        Row(
            modifier = modifier.padding(vertical = Spacing.xxxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            if (communityName.isNotEmpty()) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .onClick {
                            if (community != null) {
                                onOpenCommunity?.invoke(community)
                            }
                        },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        if (communityIcon.isNotEmpty()) {
                            val painterResource = asyncPainterResource(data = communityIcon)
                            KamelImage(
                                modifier = Modifier
                                    .padding(Spacing.xxxs)
                                    .size(iconSize)
                                    .clip(RoundedCornerShape(iconSize / 2)),
                                resource = painterResource,
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                            )
                        }
                        Text(
                            modifier = Modifier.padding(vertical = Spacing.xs),
                            text = buildString {
                                append(communityName)
                                if (communityHost.isNotEmpty()) {
                                    append("@$communityHost")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (creatorName.isNotEmpty()) {
                if (communityName.isNotEmpty()) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .onClick {
                            if (creator != null) {
                                onOpenCreator?.invoke(creator)
                            }
                        },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        if (creatorAvatar.isNotEmpty()) {
                            val painterResource = asyncPainterResource(data = creatorAvatar)
                            KamelImage(
                                modifier = Modifier
                                    .padding(Spacing.xxxs)
                                    .size(iconSize)
                                    .clip(RoundedCornerShape(iconSize / 2)),
                                resource = painterResource,
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                            )
                        }
                        Text(
                            modifier = Modifier.padding(vertical = Spacing.xs),
                            text = buildString {
                                append(creatorName)
                                if (creatorHost.isNotEmpty() && communityHost != creatorHost) {
                                    append("@$creatorHost")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
