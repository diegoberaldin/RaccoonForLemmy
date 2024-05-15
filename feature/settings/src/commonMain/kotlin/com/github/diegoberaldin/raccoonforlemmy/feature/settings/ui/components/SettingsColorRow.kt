package com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick

@Composable
internal fun SettingsColorRow(
    title: String,
    value: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onTap: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .padding(vertical = Spacing.s, horizontal = Spacing.m)
            .onClick(
                onClick = {
                    onTap?.invoke()
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(start = Spacing.xs)
                .size(36.dp)
                .background(color = value, shape = CircleShape),
        )
    }
}
