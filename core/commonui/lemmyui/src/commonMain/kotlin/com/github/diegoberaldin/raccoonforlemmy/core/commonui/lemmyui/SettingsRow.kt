package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback

@Composable
fun SettingsRow(
    icon: ImageVector? = null,
    title: String,
    value: String = "",
    disclosureIndicator: Boolean = false,
    annotatedValue: AnnotatedString = AnnotatedString(""),
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onTap: (() -> Unit)? = null,
) {
    val fullColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
    Row(
        modifier = modifier
            .padding(vertical = Spacing.s, horizontal = Spacing.m)
            .onClick(
                onClick = rememberCallback {
                    onTap?.invoke()
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        val imageModifier = Modifier.size(IconSize.m)
        if (icon != null) {
            Icon(
                modifier = imageModifier,
                imageVector = icon,
                contentDescription = null,
                tint = fullColor,
            )
        }
        Column(
            modifier = Modifier.weight(1f)
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
        if (annotatedValue.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(start = Spacing.xs),
                text = annotatedValue,
                style = MaterialTheme.typography.bodyMedium,
                color = fullColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                modifier = Modifier.padding(start = Spacing.xs),
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = fullColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (disclosureIndicator) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                tint = fullColor,
                contentDescription = null,
            )
        }
    }
}
