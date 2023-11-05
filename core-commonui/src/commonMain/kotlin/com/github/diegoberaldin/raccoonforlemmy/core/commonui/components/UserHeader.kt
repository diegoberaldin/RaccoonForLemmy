package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.outlined.MoreVert
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
import com.github.diegoberaldin.raccoonforlemmy.core.utils.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
fun UserHeader(
    user: UserModel,
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
        val banner = user.banner.orEmpty()
        if (banner.isNotEmpty() && autoLoadImages) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(4f),
            ) {
                CustomImage(
                    modifier = Modifier.fillMaxSize(),
                    url = banner,
                    quality = FilterQuality.Low,
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null,
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
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
            // options menu
            if (options.isNotEmpty()) {
                var optionsExpanded by remember { mutableStateOf(false) }
                var optionsOffset by remember { mutableStateOf(Offset.Zero) }
                Icon(
                    modifier = Modifier.onGloballyPositioned {
                        optionsOffset = it.positionInParent()
                    }.onClick(
                        rememberCallback {
                            optionsExpanded = true
                        },
                    ),
                    imageVector = Icons.Outlined.MoreVert,
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
                    ),
                ) {
                    options.forEachIndexed { idx, option ->
                        Text(
                            modifier = Modifier.padding(
                                horizontal = Spacing.m,
                                vertical = Spacing.xs,
                            ).onClick(
                                rememberCallback {
                                    optionsExpanded = false
                                    onOptionSelected?.invoke(idx)
                                },
                            ),
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
            // avatar
            val userAvatar = user.avatar.orEmpty()
            val avatarSize = 60.dp
            if (userAvatar.isNotEmpty() && autoLoadImages) {
                CustomImage(
                    modifier = Modifier.padding(Spacing.xxxs)
                        .size(avatarSize)
                        .clip(RoundedCornerShape(avatarSize / 2))
                        .onClick(
                            rememberCallback {
                                onOpenImage?.invoke(userAvatar)
                            },
                        ),
                    url = userAvatar,
                    quality = FilterQuality.Low,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                PlaceholderImage(
                    size = avatarSize,
                    title = user.name,
                )
            }

            // textual data
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Text(
                    text = buildString {
                        if (user.displayName.isNotEmpty()) {
                            append(user.displayName)
                        } else {
                            append(user.name)
                        }
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = buildString {
                        append(user.name)
                        append("@")
                        append(user.host)
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
                    val postScore = user.score?.postScore
                    val commentScore = user.score?.commentScore
                    if (postScore != null) {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Default.Padding,
                            contentDescription = null
                        )
                        Text(
                            text = postScore.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    if (commentScore != null) {
                        if (postScore != null) {
                            Spacer(modifier = Modifier.width(Spacing.xxxs))
                        }
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Default.Reply,
                            contentDescription = null
                        )
                        Text(
                            text = commentScore.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    if (user.accountAge.isNotEmpty()) {
                        if (postScore != null || commentScore != null) {
                            Spacer(modifier = Modifier.width(Spacing.xxxs))
                        }
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = Icons.Default.Cake,
                            contentDescription = null
                        )
                        Text(
                            text = user.accountAge.prettifyDate(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
