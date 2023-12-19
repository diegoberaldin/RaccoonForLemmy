package com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing

@Composable
internal fun SettingsSwitchRow(
    title: String,
    value: Boolean,
    subtitle: String? = null,
    onValueChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = Spacing.m),
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
        Switch(
            modifier = Modifier.padding(start = Spacing.xs),
            checked = value,
            onCheckedChange = {
                onValueChanged(it)
            }
        )
    }
}
