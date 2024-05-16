package com.github.diegoberaldin.raccoonforlemmy.unit.chat.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
internal fun MessageCard(
    modifier: Modifier = Modifier,
    isMyMessage: Boolean = false,
    content: String = "",
    date: String = "",
    onOpenCommunity: ((CommunityModel, String) -> Unit)? = null,
    onOpenUser: ((UserModel, String) -> Unit)? = null,
    onOpenPost: ((PostModel, String) -> Unit)? = null,
    onOpenWeb: ((String) -> Unit)? = null,
    onOpenImage: ((String) -> Unit)? = null,
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    val color =
        if (isMyMessage) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    val longDistance = Spacing.l
    val mediumDistance = Spacing.s
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)
    var optionsExpanded by remember { mutableStateOf(false) }
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier =
            modifier.padding(
                horizontal = Spacing.xs,
                vertical = Spacing.xs,
            ),
    ) {
        Canvas(
            modifier =
                Modifier
                    .size(mediumDistance)
                    .then(
                        if (isMyMessage) {
                            Modifier.align(Alignment.TopEnd)
                        } else {
                            Modifier.align(Alignment.TopStart)
                        },
                    ),
        ) {
            if (isMyMessage) {
                val path =
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(0f, size.height)
                        close()
                    }
                drawPath(path = path, color = color)
            } else {
                val path =
                    Path().apply {
                        moveTo(size.width, 0f)
                        lineTo(0f, 0f)
                        lineTo(size.width, size.height)
                        close()
                    }
                drawPath(path = path, color = color)
            }
        }
        Box(
            modifier =
                Modifier.then(
                    if (isMyMessage) {
                        Modifier.padding(start = longDistance, end = mediumDistance)
                    } else {
                        Modifier.padding(end = longDistance, start = mediumDistance)
                    },
                ).background(
                    color = color,
                    shape =
                        RoundedCornerShape(
                            topStart = if (isMyMessage) CornerSize.m else 0.dp,
                            topEnd = if (isMyMessage) 0.dp else CornerSize.m,
                            bottomStart = CornerSize.m,
                            bottomEnd = CornerSize.m,
                        ),
                ).fillMaxWidth().padding(Spacing.s),
        ) {
            CustomizedContent(ContentFontClass.Body) {
                Column {
                    PostCardBody(
                        text = content,
                        onOpenImage = onOpenImage,
                        onOpenCommunity = onOpenCommunity,
                        onOpenUser = onOpenUser,
                        onOpenPost = onOpenPost,
                        onOpenWeb = onOpenWeb,
                    )
                    Box {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (options.isNotEmpty()) {
                                Icon(
                                    modifier =
                                        Modifier
                                            .size(IconSize.m)
                                            .padding(Spacing.xs)
                                            .onGloballyPositioned {
                                                optionsOffset = it.positionInParent()
                                            }
                                            .onClick(
                                                onClick = {
                                                    optionsExpanded = true
                                                },
                                            ),
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = null,
                                    tint = ancillaryColor,
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))

                            if (date.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        modifier = Modifier.size(IconSize.s).padding(0.5.dp),
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = ancillaryColor,
                                    )
                                    Text(
                                        modifier = Modifier.padding(start = Spacing.xxs),
                                        text = date.prettifyDate(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = ancillaryColor,
                                    )
                                }
                            } else {
                                Text(
                                    text = "",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                        CustomDropDown(
                            expanded = optionsExpanded,
                            onDismiss = {
                                optionsExpanded = false
                            },
                            offset =
                                DpOffset(
                                    x = optionsOffset.x.toLocalDp(),
                                    y = optionsOffset.y.toLocalDp(),
                                ),
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(option.text)
                                    },
                                    onClick = {
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
}
