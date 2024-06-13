package com.github.diegoberaldin.raccoonforlemmy.unit.licences.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.unit.licences.models.LicenceItem
import com.github.diegoberaldin.raccoonforlemmy.unit.licences.models.toIcon

@Composable
internal fun LicenceItem(
    item: LicenceItem,
    modifier: Modifier = Modifier,
) {
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)

    Row(
        modifier =
            modifier.padding(
                vertical = Spacing.xs,
                horizontal = Spacing.s,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item.type?.also { type ->
            Icon(
                modifier = Modifier.size(IconSize.m),
                imageVector = type.toIcon(),
                contentDescription = null,
                tint = fullColor,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = fullColor,
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ancillaryColor,
                )
            }
        }
    }
}
