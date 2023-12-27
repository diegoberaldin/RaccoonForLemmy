package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PlaceholderImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

@Composable
fun CommunityItem(
    community: CommunityModel,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    autoLoadImages: Boolean = true,
    showSubscribers: Boolean = false,
    showFavorite: Boolean = false,
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    val title = community.title.replace("&amp;", "&")
    val communityName = community.name
    val communityIcon = community.icon.orEmpty()
    val communityHost = community.host
    val iconSize = if (small) IconSize.m else IconSize.l
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    var optionsMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(horizontal = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (communityIcon.isNotEmpty() && autoLoadImages) {
            CustomImage(
                modifier = Modifier
                    .padding(Spacing.xxxs)
                    .size(iconSize)
                    .clip(RoundedCornerShape(iconSize / 2)),
                url = communityIcon,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            PlaceholderImage(
                size = iconSize,
                title = community.name,
            )
        }

        CustomizedContent {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                val translationAmount = 3.dp.toLocalPixel()
                Text(
                    text = buildString {
                        append(title)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = fullColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    modifier = Modifier.graphicsLayer {
                        translationY = -translationAmount
                    },
                    text = buildString {
                        append("!")
                        append(communityName)
                        if (communityHost.isNotEmpty()) {
                            append("@$communityHost")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ancillaryColor,
                )
            }
            if (showSubscribers) {
                Row(
                    modifier = Modifier.padding(start = Spacing.xxs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = community.subscribers.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        if (showFavorite) {
            if (community.favorite) {
                Icon(
                    modifier = Modifier.size(IconSize.s),
                    imageVector = Icons.Default.Star,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        if (options.isNotEmpty()) {
            Box {
                Icon(
                    modifier = Modifier.size(IconSize.m)
                        .padding(Spacing.xs)
                        .onGloballyPositioned {
                            optionsOffset = it.positionInParent()
                        }
                        .onClick(
                            onClick = rememberCallback {
                                optionsMenuOpen = true
                            },
                        ),
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = ancillaryColor,
                )

                CustomDropDown(
                    expanded = optionsMenuOpen,
                    onDismiss = {
                        optionsMenuOpen = false
                    },
                    offset = DpOffset(
                        x = optionsOffset.x.toLocalDp(),
                        y = optionsOffset.y.toLocalDp(),
                    ),
                ) {
                    options.forEach { option ->
                        Text(
                            modifier = Modifier.padding(
                                horizontal = Spacing.m,
                                vertical = Spacing.s,
                            ).onClick(
                                onClick = rememberCallback {
                                    optionsMenuOpen = false
                                    onOptionSelected?.invoke(option.id)
                                },
                            ),
                            text = option.text,
                        )
                    }
                }
            }
        }
    }
}


