package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.racconforlemmy.core.utils.DateTime
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
internal fun ChatCard(
    user: UserModel?,
    lastMessage: String,
    lastMessageDate: String? = null,
    modifier: Modifier = Modifier,
    onOpenUser: ((UserModel) -> Unit)? = null,
    onOpen: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .padding(Spacing.xs)
            .onClick {
                onOpen?.invoke()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        val creatorName = user?.name.orEmpty()
        val creatorHost = user?.host.orEmpty()
        val creatorAvatar = user?.avatar.orEmpty()
        val iconSize = 46.dp

        if (creatorAvatar.isNotEmpty()) {
            val painterResource = asyncPainterResource(data = creatorAvatar)
            KamelImage(
                modifier = Modifier
                    .padding(Spacing.xxxs)
                    .size(iconSize)
                    .clip(RoundedCornerShape(iconSize / 2))
                    .onClick {
                        if (user != null) {
                            onOpenUser?.invoke(user)
                        }
                    },
                resource = painterResource,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            Box(
                modifier = Modifier
                    .padding(Spacing.xxxs)
                    .size(iconSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(iconSize / 2),
                    ).onClick {
                        if (user != null) {
                            onOpenUser?.invoke(user)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = creatorName.firstOrNull()?.toString().orEmpty().uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // last message text
            Text(
                text = lastMessage,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
            )

            // last message date
            if (lastMessageDate != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val buttonModifier = Modifier.size(24.dp).padding(3.25.dp)
                    Icon(
                        modifier = buttonModifier.padding(1.dp),
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = lastMessageDate.let {
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
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}