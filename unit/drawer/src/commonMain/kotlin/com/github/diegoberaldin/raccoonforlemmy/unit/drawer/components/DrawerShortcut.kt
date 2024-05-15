package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize

@Composable
internal fun DrawerShortcut(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onSelected: (() -> Unit)? = null,
) {
    NavigationDrawerItem(
        modifier = modifier,
        selected = false,
        icon = {
            Icon(
                modifier = Modifier.size(IconSize.m),
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        },
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        onClick = {
            onSelected?.invoke()
        },
    )
}
