package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.detail

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
import com.github.diegoberaldin.racconforlemmy.core.utils.DateTime
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
internal fun MessageCard(
    isMyMessage: Boolean = false,
    content: String = "",
    date: String = "",
) {
    val color = if (isMyMessage) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val textColor = if (isMyMessage) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onSecondary
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
            Column {
                Text(
                    text = content,
                    color = textColor,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    if (date.isNotEmpty()) {
                        val buttonModifier = Modifier.size(22.dp).padding(3.dp)
                        Icon(
                            modifier = buttonModifier.padding(1.dp),
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = textColor,
                        )
                        Text(
                            text = date.let {
                                when {
                                    it.isEmpty() -> it
                                    !it.endsWith("Z") -> {
                                        DateTime.getPrettyDate(
                                            iso8601Timestamp = it + "Z",
                                            yearLabel = stringResource(
                                                MR.strings.profile_year_short
                                            ),
                                            monthLabel = stringResource(
                                                MR.strings.profile_month_short
                                            ),
                                            dayLabel = stringResource(MR.strings.profile_day_short),
                                            hourLabel = stringResource(
                                                MR.strings.post_hour_short
                                            ),
                                            minuteLabel = stringResource(
                                                MR.strings.post_minute_short
                                            ),
                                            secondLabel = stringResource(
                                                MR.strings.post_second_short
                                            ),
                                        )
                                    }

                                    else -> {
                                        DateTime.getPrettyDate(
                                            iso8601Timestamp = it,
                                            yearLabel = stringResource(
                                                MR.strings.profile_year_short
                                            ),
                                            monthLabel = stringResource(
                                                MR.strings.profile_month_short
                                            ),
                                            dayLabel = stringResource(MR.strings.profile_day_short),
                                            hourLabel = stringResource(
                                                MR.strings.post_hour_short
                                            ),
                                            minuteLabel = stringResource(
                                                MR.strings.post_minute_short
                                            ),
                                            secondLabel = stringResource(
                                                MR.strings.post_second_short
                                            ),
                                        )
                                    }
                                }
                            },
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