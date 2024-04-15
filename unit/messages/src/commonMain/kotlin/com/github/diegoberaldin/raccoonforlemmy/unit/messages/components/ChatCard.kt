package com.github.diegoberaldin.raccoonforlemmy.unit.messages.components

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
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.DpOffset
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PlaceholderImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableName

@Composable
internal fun ChatCard(
    user: UserModel?,
    autoLoadImages: Boolean = true,
    preferNicknames: Boolean = true,
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
    val creatorName = user?.readableName(preferNicknames).orEmpty()
    val creatorAvatar = user?.avatar.orEmpty()
    val iconSize = IconSize.xl
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)

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
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Row(
                modifier = Modifier.padding(end = Spacing.m),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // user name
                Text(
                    modifier = Modifier.weight(1f),
                    text = creatorName,
                    style = MaterialTheme.typography.bodySmall,
                    color = ancillaryColor,
                )
            }
            CustomizedContent(ContentFontClass.Body) {
                // last message text
                PostCardBody(
                    maxLines = 2,
                    text = lastMessage,
                    autoLoadImages = autoLoadImages,
                    onClick = rememberCallback {
                        onOpen?.invoke()
                    },
                )
            }

            // last message date
            if (lastMessageDate != null) {
                Box {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier.size(IconSize.s),
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = ancillaryColor,
                            )
                            Text(
                                modifier = Modifier.padding(start = Spacing.xxs),
                                text = lastMessageDate.prettifyDate(),
                                style = MaterialTheme.typography.bodySmall,
                                color = ancillaryColor,
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (options.isNotEmpty()) {
                            Icon(
                                modifier = Modifier.size(IconSize.m)
                                    .padding(Spacing.xs)
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
                                tint = ancillaryColor,
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
                            DropdownMenuItem(
                                text = {
                                    Text(option.text)
                                },
                                onClick = rememberCallback {
                                    optionsExpanded = false
                                    onOptionSelected?.invoke(option.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
