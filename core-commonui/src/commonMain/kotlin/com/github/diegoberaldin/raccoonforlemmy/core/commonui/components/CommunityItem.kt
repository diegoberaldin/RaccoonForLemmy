package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

@Composable
fun CommunityItem(
    community: CommunityModel,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    autoLoadImages: Boolean = true,
) {
    val title = community.title.replace("&amp;", "&")
    val communityName = community.name
    val communityIcon = community.icon.orEmpty()
    val communityHost = community.host
    val iconSize = if (small) 24.dp else 30.dp
    Row(
        modifier = modifier.padding(
            vertical = Spacing.xs,
            horizontal = Spacing.s,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (communityIcon.isNotEmpty() && autoLoadImages) {
            CustomImage(
                modifier = Modifier
                    .padding(Spacing.xxxs)
                    .size(iconSize)
                    .clip(RoundedCornerShape(iconSize / 2)),
                url = communityIcon,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            PlaceholderImage(
                size = iconSize,
                title = community.name,
            )
        }
        ScaledContent {
            Column(
                modifier = Modifier.padding(start = Spacing.xs),
            ) {
                Text(
                    text = buildString {
                        append(title)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = buildString {
                        append("!")
                        append(communityName)
                        if (communityHost.isNotEmpty()) {
                            append("@$communityHost")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}


