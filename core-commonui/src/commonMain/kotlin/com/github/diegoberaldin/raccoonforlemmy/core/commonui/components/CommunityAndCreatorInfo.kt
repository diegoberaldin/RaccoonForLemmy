package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
fun CommunityAndCreatorInfo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 32.dp,
    indicatorExpanded: Boolean? = null,
    autoLoadImages: Boolean = true,
    community: CommunityModel? = null,
    creator: UserModel? = null,
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onToggleExpanded: (() -> Unit)? = null,
) {
    val communityName = community?.name.orEmpty()
    val communityIcon = community?.icon.orEmpty()
    val communityHost = community?.host.orEmpty()
    val creatorName = creator?.name.orEmpty()
    val creatorAvatar = creator?.avatar.orEmpty()
    val creatorHost = creator?.host.orEmpty()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        if (communityIcon.isNotEmpty()) {
            if (autoLoadImages) {
                CustomImage(
                    modifier = Modifier
                        .onClick(
                            rememberCallback {
                                if (community != null) {
                                    onOpenCommunity?.invoke(community)
                                }
                            },
                        )
                        .padding(Spacing.xxxs)
                        .size(iconSize)
                        .clip(RoundedCornerShape(iconSize / 2)),
                    url = communityIcon,
                    quality = FilterQuality.Low,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                PlaceholderImage(
                    modifier = Modifier.onClick(
                        rememberCallback {
                            if (community != null) {
                                onOpenCommunity?.invoke(community)
                            }
                        },
                    ),
                    size = 32.dp,
                    title = communityName,
                )
            }
        } else if (creatorAvatar.isNotEmpty()) {
            if (autoLoadImages) {
                CustomImage(
                    modifier = Modifier
                        .onClick(
                            rememberCallback {
                                if (creator != null) {
                                    onOpenCreator?.invoke(creator)
                                }
                            },
                        )
                        .padding(Spacing.xxxs)
                        .size(iconSize)
                        .clip(RoundedCornerShape(iconSize / 2)),
                    url = creatorAvatar,
                    quality = FilterQuality.Low,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                PlaceholderImage(
                    modifier = Modifier.onClick(
                        rememberCallback {
                            if (creator != null) {
                                onOpenCreator?.invoke(creator)
                            }
                        },
                    ),
                    size = iconSize,
                    title = creatorName,
                )
            }
        }
        Column(
            modifier = Modifier.padding(vertical = Spacing.xxxs),
        ) {
            if (community != null) {
                Text(
                    modifier = Modifier
                        .onClick(
                            rememberCallback {
                                onOpenCommunity?.invoke(community)
                            },
                        ),
                    text = buildString {
                        append(communityName)
                        if (communityHost.isNotEmpty()) {
                            append("@$communityHost")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (creator != null) {
                Text(
                    modifier = Modifier
                        .onClick(
                            rememberCallback {
                                onOpenCreator?.invoke(creator)
                            },
                        ),
                    text = buildString {
                        append(creatorName)
                        if (creatorHost.isNotEmpty()) {
                            append("@$creatorHost")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        if (indicatorExpanded != null) {
            Spacer(modifier = Modifier.weight(1f))
            val expandedModifier = Modifier
                .padding(end = Spacing.xs)
                .onClick(
                    rememberCallback {
                        onToggleExpanded?.invoke()
                    },
                )
            if (indicatorExpanded) {
                Icon(
                    modifier = expandedModifier,
                    imageVector = Icons.Default.ExpandLess,
                    contentDescription = null,
                )
            } else {
                Icon(
                    modifier = expandedModifier,
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            }
        }
    }
}
