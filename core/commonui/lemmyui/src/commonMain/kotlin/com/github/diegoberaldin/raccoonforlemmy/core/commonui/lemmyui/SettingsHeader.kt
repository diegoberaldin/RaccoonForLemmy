package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing

@Composable
fun SettingsHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    rightButton: @Composable (() -> Unit)? = null,
) {
    val fullColor = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = modifier.padding(
            top = Spacing.xxs,
            bottom = Spacing.xxxs,
            start = Spacing.s,
            end = Spacing.s,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier.size(IconSize.m),
                imageVector = icon,
                contentDescription = null,
                tint = fullColor,
            )
        }
        Text(
            text = title,
            color = fullColor,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (rightButton != null) {
            rightButton()
        }
    }
}