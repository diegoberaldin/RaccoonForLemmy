package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.racconforlemmy.core.utils.DateTime
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun PostCardFooter(
    comments: Int? = null,
    date: String? = null,
    score: Int,
    saved: Boolean = false,
    upVoted: Boolean = false,
    downVoted: Boolean = false,
    onUpVote: (() -> Unit)? = null,
    onDownVote: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onReply: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.padding(bottom = Spacing.xxxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        val buttonModifier = Modifier.size(28.dp).padding(3.5.dp)
        if (comments != null) {
            Image(
                modifier = buttonModifier.padding(1.dp).onClick {
                    onReply?.invoke()
                },
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface),
            )
            Text(
                modifier = Modifier.padding(end = Spacing.s),
                text = "$comments",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (date != null) {
            Icon(
                modifier = buttonModifier.padding(1.dp),
                imageVector = Icons.Default.HourglassBottom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = date.let {
                    when {
                        it.isEmpty() -> it
                        !it.endsWith("Z") -> {
                            DateTime.getPrettyDate(
                                iso8601Timestamp = it + "Z",
                                yearLabel = stringResource(MR.strings.profile_year_short),
                                monthLabel = stringResource(MR.strings.profile_month_short),
                                dayLabel = stringResource(MR.strings.profile_day_short),
                                hourLabel = stringResource(MR.strings.post_hour_short),
                                minuteLabel = stringResource(MR.strings.post_minute_short),
                                secondLabel = stringResource(MR.strings.post_second_short),
                            )
                        }

                        else -> {
                            DateTime.getPrettyDate(
                                iso8601Timestamp = it,
                                yearLabel = stringResource(MR.strings.profile_year_short),
                                monthLabel = stringResource(MR.strings.profile_month_short),
                                dayLabel = stringResource(MR.strings.profile_day_short),
                                hourLabel = stringResource(MR.strings.post_hour_short),
                                minuteLabel = stringResource(MR.strings.post_minute_short),
                                secondLabel = stringResource(MR.strings.post_second_short),
                            )
                        }
                    }
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Image(
            modifier = buttonModifier.onClick {
                onSave?.invoke()
            },
            imageVector = if (!saved) {
                Icons.Default.BookmarkBorder
            } else {
                Icons.Default.Bookmark
            },
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                color = if (saved) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ),
        )
        Image(
            modifier = buttonModifier
                .let {
                    if (upVoted) {
                        it.background(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape,
                        )
                    } else {
                        it
                    }
                }.onClick {
                    onUpVote?.invoke()
                },
            imageVector = if (upVoted) {
                Icons.Default.ArrowCircleUp
            } else {
                Icons.Default.ArrowCircleUp
            },
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                color = if (upVoted) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ),
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Image(
            modifier = buttonModifier
                .let {
                    if (downVoted) {
                        it.background(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape,
                        )
                    } else {
                        it
                    }
                }
                .onClick {
                    onDownVote?.invoke()
                },
            imageVector = if (downVoted) {
                Icons.Default.ArrowCircleDown
            } else {
                Icons.Default.ArrowCircleDown
            },
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                color = if (downVoted) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ),
        )
    }
}
