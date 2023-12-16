package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Composable
fun UserItem(
    user: UserModel,
    modifier: Modifier = Modifier,
    autoLoadImages: Boolean = true,
) {
    val name = user.name
    val avatar = user.avatar.orEmpty()
    val host = user.host
    val iconSize = 30.dp
    Row(
        modifier = modifier.padding(
            vertical = Spacing.xs,
            horizontal = Spacing.s,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (avatar.isNotEmpty() && autoLoadImages) {
            CustomImage(
                modifier = Modifier.padding(Spacing.xxxs).size(iconSize)
                    .clip(RoundedCornerShape(iconSize / 2)),
                url = avatar,
                quality = FilterQuality.Low,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            PlaceholderImage(
                size = iconSize,
                title = name,
            )
        }

        CustomizedContent {
            Text(
                text = buildString {
                    append(name)
                    if (host.isNotEmpty()) {
                        append("@$host")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
