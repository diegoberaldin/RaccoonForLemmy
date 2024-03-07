package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FeedbackButton

@Composable
fun SettingsIntValueRow(
    title: String,
    value: Int,
    subtitle: String? = null,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedbackButton(
                imageVector = Icons.Default.ArrowCircleDown,
                tintColor = MaterialTheme.colorScheme.secondary,
                onClick = onDecrement,
            )
            Text(
                modifier = Modifier
                    .sizeIn(minWidth = 40.dp)
                    .padding(horizontal = Spacing.s),
                textAlign = TextAlign.Center,
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = fullColor,
            )
            FeedbackButton(
                imageVector = Icons.Default.ArrowCircleUp,
                tintColor = MaterialTheme.colorScheme.secondary,
                onClick = onIncrement,
            )
        }
    }
}
