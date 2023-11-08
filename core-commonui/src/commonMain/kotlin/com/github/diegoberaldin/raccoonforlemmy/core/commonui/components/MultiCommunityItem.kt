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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel

@Composable
fun MultiCommunityItem(
    community: MultiCommunityModel,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    autoLoadImages: Boolean = true,
) {
    val title = community.name
    val communityIcon = community.icon.orEmpty()
    val iconSize = if (small) IconSize.m else IconSize.l
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
                modifier = Modifier.padding(Spacing.xxxs).size(iconSize)
                    .clip(RoundedCornerShape(iconSize / 2)),
                url = communityIcon,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            PlaceholderImage(
                size = iconSize,
                title = title,
            )
        }
        Column(
            modifier = Modifier.padding(start = Spacing.xs),
        ) {
            Text(
                modifier = Modifier.padding(vertical = Spacing.s),
                text = buildString {
                    append(title)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
