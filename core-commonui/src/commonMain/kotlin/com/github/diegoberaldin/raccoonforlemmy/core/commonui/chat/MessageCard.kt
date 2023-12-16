package com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat

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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp

@Composable
internal fun MessageCard(
    isMyMessage: Boolean = false,
    content: String = "",
    date: String = "",
    options: List<Option> = emptyList(),
    onOptionSelected: ((OptionId) -> Unit)? = null,
) {
    val color = if (isMyMessage) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = if (isMyMessage) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    val longDistance = Spacing.l
    val mediumDistance = Spacing.s
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
    var optionsExpanded by remember { mutableStateOf(false) }
    var optionsOffset by remember { mutableStateOf(Offset.Zero) }

    Box {
        Canvas(
            modifier = Modifier.size(mediumDistance).let {
                if (isMyMessage) {
                    it.align(Alignment.TopEnd)
                } else {
                    it.align(Alignment.TopStart)
                }
            }
        ) {
            if (isMyMessage) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path = path, color = color)
            } else {
                val path = Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(0f, 0f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(path = path, color = color)
            }
        }
        Box(
            modifier = Modifier.let {
                if (isMyMessage) {
                    it.padding(start = longDistance, end = mediumDistance)
                } else {
                    it.padding(end = longDistance, start = mediumDistance)
                }
            }.background(
                color = color, shape = RoundedCornerShape(
                    topStart = if (isMyMessage) CornerSize.m else 0.dp,
                    topEnd = if (isMyMessage) 0.dp else CornerSize.m,
                    bottomStart = CornerSize.m,
                    bottomEnd = CornerSize.m,
                )
            ).fillMaxWidth().padding(Spacing.s)
        ) {
            CustomizedContent {
                Column {
                    PostCardBody(
                        text = content,
                    )
                    Box {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
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
                                    tint = ancillaryColor
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))

                            if (date.isNotEmpty()) {
                                val buttonModifier = Modifier.size(IconSize.m).padding(3.5.dp)
                                Icon(
                                    modifier = buttonModifier,
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = ancillaryColor,
                                )
                                Text(
                                    text = date.prettifyDate(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ancillaryColor,
                                )
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
}