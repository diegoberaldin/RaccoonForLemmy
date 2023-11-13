package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.graphics.FilterQuality
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
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PlaceholderImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ScaledContent
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
internal fun ChatCard(
    user: UserModel?,
    autoLoadImages: Boolean = true,
    lastMessage: String,
    lastMessageDate: String? = null,
    modifier: Modifier = Modifier,
    options: List<Option> = emptyList(),
    onOpenUser: ((UserModel) -> Unit)? = null,
    onOpen: (() -> Unit)? = null,
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    var optionsExpanded by remember { mutableStateOf(false) }
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }
    val creatorName = user?.name.orEmpty()
    val creatorHost = user?.host.orEmpty()
    val creatorAvatar = user?.avatar.orEmpty()
    val iconSize = IconSize.xl

    Row(
        modifier = modifier
            .padding(Spacing.xs)
            .onClick(
                onClick = rememberCallback {
                    onOpen?.invoke()
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {


        if (creatorAvatar.isNotEmpty()) {
            CustomImage(
                modifier = Modifier
                    .padding(Spacing.xxxs)
                    .size(iconSize)
                    .clip(RoundedCornerShape(iconSize / 2))
                    .onClick(
                        onClick = rememberCallback {
                            if (user != null) {
                                onOpenUser?.invoke(user)
                            }
                        },
                    ),
                quality = FilterQuality.Low,
                url = creatorAvatar,
                autoload = autoLoadImages,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            PlaceholderImage(
                modifier = Modifier.onClick(
                    onClick = rememberCallback {
                        if (user != null) {
                            onOpenUser?.invoke(user)
                        }
                    },
                ),
                size = iconSize,
                title = creatorName,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            // user name
            Text(
                text = buildString {
                    append(creatorName)
                    if (creatorHost.isNotEmpty()) {
                        append("@$creatorHost")
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ScaledContent {
                // last message text
                Text(
                    text = lastMessage,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                )
            }

            // last message date
            if (lastMessageDate != null) {
                Box {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val buttonModifier = Modifier.size(IconSize.m).padding(3.5.dp)
                        Icon(
                            modifier = buttonModifier.padding(1.dp),
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = lastMessageDate.prettifyDate(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (options.isNotEmpty()) {
                            Icon(
                                modifier = buttonModifier
                                    .padding(top = Spacing.xxs)
                                    .onGloballyPositioned {
                                        optionsOffset = it.positionInParent()
                                    }
                                    .onClick(
                                        onClick = rememberCallback {
                                            optionsExpanded = true
                                        },
                                    ),
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = null,
                            )
                        }
                    }

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
                        options.forEach { option ->
                            Text(
                                modifier = Modifier.padding(
                                    horizontal = Spacing.m,
                                    vertical = Spacing.s,
                                ).onClick(
                                    onClick = rememberCallback {
                                        optionsExpanded = false
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
}