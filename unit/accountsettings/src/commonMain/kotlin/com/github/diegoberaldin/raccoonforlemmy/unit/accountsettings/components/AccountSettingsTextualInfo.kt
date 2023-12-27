package com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback

@Composable
internal fun AccountSettingsTextualInfo(
    modifier: Modifier = Modifier,
    title: String = "",
    value: String = "",
    valueStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    onEdit: (() -> Unit)? = null,
) {
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = Spacing.xs,
                horizontal = Spacing.m,
            ).onClick(
                onClick = rememberCallback {
                    onEdit?.invoke()
                },
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = ancillaryColor,
            )
            CustomizedContent {
                Text(
                    text = value,
                    style = valueStyle,
                    color = fullColor,
                )
            }
        }
    }
}