package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha

@Composable
fun SettingsSwitchRow(
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
            val fullColor = MaterialTheme.colorScheme.onBackground
            val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = fullColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = ancillaryColor,
                )
            }
        }
        Switch(
            modifier = Modifier.padding(start = Spacing.xs),
            checked = value,
            onCheckedChange = {
                onValueChanged(it)
            },
        )
    }
}
