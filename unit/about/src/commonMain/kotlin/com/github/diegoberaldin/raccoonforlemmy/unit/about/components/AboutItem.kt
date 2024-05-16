package com.github.diegoberaldin.raccoonforlemmy.unit.about.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick

@Composable
internal fun AboutItem(
    painter: Painter? = null,
    vector: ImageVector? = null,
    text: String,
    textDecoration: TextDecoration = TextDecoration.None,
    value: String = "",
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier.padding(
                horizontal = Spacing.xs,
                vertical = Spacing.s,
            ).onClick(
                onClick = {
                    onClick?.invoke()
                },
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val imageModifier = Modifier.size(22.dp)
        if (painter != null) {
            Image(
                modifier = imageModifier,
                painter = painter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )
        } else if (vector != null) {
            Image(
                modifier = imageModifier,
                imageVector = vector,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = textDecoration,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
