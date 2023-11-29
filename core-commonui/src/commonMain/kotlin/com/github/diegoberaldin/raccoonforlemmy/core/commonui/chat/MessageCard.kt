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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ScaledContent
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.prettifyDate

@Composable
internal fun MessageCard(
    isMyMessage: Boolean = false,
    content: String = "",
    date: String = "",
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
            ScaledContent {
                Column {
                    PostCardBody(
                        text = content,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        if (date.isNotEmpty()) {
                            val buttonModifier = Modifier.size(IconSize.m).padding(3.5.dp)
                            Icon(
                                modifier = buttonModifier,
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = textColor,
                            )
                            Text(
                                text = date.prettifyDate(),
                                style = MaterialTheme.typography.labelMedium,
                                color = textColor,
                            )
                        } else {
                            Text(
                                text = "",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}