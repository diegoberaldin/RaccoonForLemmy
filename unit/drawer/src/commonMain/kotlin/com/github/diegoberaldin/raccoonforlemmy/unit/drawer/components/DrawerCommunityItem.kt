package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PlaceholderImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback

@Composable
internal fun DrawerCommunityItem(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    url: String? = null,
    favorite: Boolean = false,
    autoLoadImages: Boolean = true,
    onSelected: (() -> Unit)? = null,
) {
    NavigationDrawerItem(
        modifier = modifier,
        selected = false,
        icon = {
            val iconSize = IconSize.m
            if (!url.isNullOrEmpty() && autoLoadImages) {
                CustomImage(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(RoundedCornerShape(iconSize / 2)),
                    url = url,
                    contentScale = ContentScale.FillBounds,
                )
            } else {
                PlaceholderImage(
                    size = iconSize,
                    title = title,
                )
            }
        },
        label = {
            val fullColor = MaterialTheme.colorScheme.onBackground
            val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)
            Column {
                Text(
                    text = title,
                    color = fullColor,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                subtitle.takeIf { it != title }?.also { subtitle ->
                    Text(
                        text = subtitle,
                        color = ancillaryColor,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                }
            }
        },
        badge = if (favorite) {
            @Composable {
                Icon(
                    modifier = Modifier.size(IconSize.s),
                    imageVector = Icons.Default.Star,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        } else {
            null
        },
        onClick = {
            onSelected?.invoke()
        },
    )
}
