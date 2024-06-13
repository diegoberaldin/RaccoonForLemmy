package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.components

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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PlaceholderImage
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.models.AcknowledgementModel

@Composable
fun AcknoledgementItem(
    item: AcknowledgementModel,
    modifier: Modifier = Modifier,
) {
    val title = item.title.orEmpty()
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)
    val iconSize = IconSize.xl

    Row(
        modifier =
            modifier.padding(
                vertical = Spacing.xs,
                horizontal = Spacing.s,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!item.avatar.isNullOrEmpty()) {
            item.avatar.also { url ->
                CustomImage(
                    modifier =
                        Modifier
                            .size(iconSize)
                            .clip(RoundedCornerShape(iconSize / 2)),
                    contentDescription = null,
                    url = url,
                )
            }
        } else {
            PlaceholderImage(
                size = iconSize,
                title = title,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = fullColor,
            )
            if (!item.subtitle.isNullOrEmpty()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ancillaryColor,
                )
            }
        }
    }
}
