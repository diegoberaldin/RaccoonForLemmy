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
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
fun CommunityAndCreatorInfo(
    modifier: Modifier = Modifier,
    iconSize: Dp = IconSize.l,
    indicatorExpanded: Boolean? = null,
    autoLoadImages: Boolean = true,
    community: CommunityModel? = null,
    creator: UserModel? = null,
    distinguished: Boolean = false,
    featured: Boolean = false,
    locked: Boolean = false,
    isFromModerator: Boolean = false,
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
    onOpenCreator: ((UserModel) -> Unit)? = null,
    onToggleExpanded: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
) {
    val communityName = community?.name.orEmpty()
    val communityIcon = community?.icon.orEmpty()
    val communityHost = community?.host.orEmpty()
    val creatorName = creator?.name.orEmpty()
    val creatorAvatar = creator?.avatar.orEmpty()
    val creatorHost = creator?.host.orEmpty()
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)

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
                            onClick = rememberCallback {
                                if (community != null) {
                                    onOpenCommunity?.invoke(community)
                                }
                            },
                            onDoubleClick = onDoubleClick ?: {},
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
                        onClick = rememberCallback {
                            if (community != null) {
                                onOpenCommunity?.invoke(community)
                            }
                        },
                        onDoubleClick = onDoubleClick ?: {},
                    ),
                    size = IconSize.l,
                    title = communityName,
                )
            }
        } else if (creatorAvatar.isNotEmpty()) {
            if (autoLoadImages) {
                CustomImage(
                    modifier = Modifier
                        .onClick(
                            onClick = rememberCallback {
                                if (creator != null) {
                                    onOpenCreator?.invoke(creator)
                                }
                            },
                            onDoubleClick = onDoubleClick ?: {},
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
                        onClick = rememberCallback {
                            if (creator != null) {
                                onOpenCreator?.invoke(creator)
                            }
                        },
                        onDoubleClick = onDoubleClick ?: {},
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
                            onClick = rememberCallback {
                                onOpenCommunity?.invoke(community)
                            },
                            onDoubleClick = onDoubleClick ?: {},
                        ),
                    text = buildString {
                        append(communityName)
                        if (communityHost.isNotEmpty()) {
                            append("@$communityHost")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (creator == null) ancillaryColor else fullColor,
                )
            }
            if (creator != null) {
                val translationAmount = 3.dp.toLocalPixel()
                Text(
                    modifier = Modifier
                        .graphicsLayer {
                            if (communityName.isNotEmpty()) {
                                translationY = -translationAmount
                            }
                        }
                        .onClick(
                            onClick = rememberCallback {
                                onOpenCreator?.invoke(creator)
                            },
                            onDoubleClick = onDoubleClick ?: {},
                        ),
                    text = buildString {
                        append(creatorName)
                        if (creatorHost.isNotEmpty()) {
                            append("@$creatorHost")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ancillaryColor,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        val buttonModifier = Modifier.size(IconSize.m).padding(3.5.dp)
        if (isFromModerator) {
            Icon(
                modifier = buttonModifier,
                imageVector = Icons.Default.LocalPolice,
                contentDescription = null,
            )
        }
        if (distinguished) {
            Icon(
                modifier = buttonModifier,
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
            )
        } else if (featured) {
            Icon(
                modifier = buttonModifier,
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
            )
        }
        if (locked) {
            Icon(
                modifier = buttonModifier,
                imageVector = Icons.Default.Lock,
                contentDescription = null,
            )
        }
        if (indicatorExpanded != null) {
            val expandedModifier = Modifier
                .padding(end = Spacing.xs)
                .onClick(
                    onClick = rememberCallback {
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
