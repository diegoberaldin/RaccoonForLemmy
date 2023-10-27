package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

@Composable
fun CommunityHeader(
    community: CommunityModel,
    modifier: Modifier = Modifier,
    autoLoadImages: Boolean = true,
    options: List<String> = emptyList(),
    onOptionSelected: ((Int) -> Unit)? = null,
    onOpenImage: ((String) -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        // banner
        val banner = community.banner.orEmpty()
        if (banner.isNotEmpty() && autoLoadImages) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(4f),
            ) {
                CustomImage(
                    modifier = Modifier.fillMaxSize(),
                    url = banner,
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                                ),
                            ),
                        ),
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = Spacing.xs, end = Spacing.s).align(Alignment.TopEnd)
        ) {
            if (options.isNotEmpty()) {
                var optionsExpanded by remember { mutableStateOf(false) }
                var optionsOffset by remember { mutableStateOf(Offset.Zero) }
                Icon(
                    modifier = Modifier.onGloballyPositioned {
                        optionsOffset = it.positionInParent()
                    }.onClick {
                        optionsExpanded = true
                    },
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                )
                CustomDropDown(
                    expanded = optionsExpanded,
                    onDismiss = {
                        optionsExpanded = false
                    },
                    offset = DpOffset(
                        x = optionsOffset.x.toLocalDp(),
                        y = optionsOffset.y.toLocalDp(),
                        // y = (-50).dp,
                    ),
                ) {
                    options.forEachIndexed { idx, option ->
                        Text(
                            modifier = Modifier.padding(
                                horizontal = Spacing.m,
                                vertical = Spacing.xs,
                            ).onClick {
                                optionsExpanded = false
                                onOptionSelected?.invoke(idx)
                            },
                            text = option,
                        )
                    }
                }
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.s).align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            val title = community.title.replace("&amp;", "&")
            val communityName = community.name
            val communityIcon = community.icon.orEmpty()
            val communityHost = community.host
            val avatarSize = 60.dp

            // avatar
            if (communityIcon.isNotEmpty() && autoLoadImages) {
                CustomImage(
                    modifier = Modifier
                        .padding(Spacing.xxxs)
                        .size(avatarSize)
                        .clip(RoundedCornerShape(avatarSize / 2))
                        .onClick {
                            onOpenImage?.invoke(communityIcon)
                        },
                    url = communityIcon,
                    quality = FilterQuality.Low,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                PlaceholderImage(
                    size = avatarSize,
                    title = communityName,
                )
            }

            // textual data
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Text(
                    text = title.replace("&amp;", "&"),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Text(
                    modifier = Modifier.padding(horizontal = Spacing.s),
                    text = buildString {
                        append(communityName)
                        if (communityHost.isNotEmpty()) {
                            append("@$communityHost")
                        }
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // stats and age
                val iconSize = 22.dp
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (community.subscribers >= 0) {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Default.Group,
                            contentDescription = null
                        )
                        Text(
                            text = community.subscribers.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    if (community.monthlyActiveUsers >= 0) {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Default.CalendarViewMonth,
                            contentDescription = null
                        )
                        Text(
                            text = community.monthlyActiveUsers.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
