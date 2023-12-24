package com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PlaceholderImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

@Composable
internal fun ModeratedCommunityCell(
    community: CommunityModel,
    autoLoadImages: Boolean = true,
    onOpenCommunity: ((CommunityModel) -> Unit)? = null,
) {
    val name = community.name
    val host = community.host
    val icon = community.icon.orEmpty()
    val iconSize = IconSize.xl
    val fullTextColor = MaterialTheme.colorScheme.onBackground
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon.isNotEmpty()) {
            CustomImage(
                modifier = Modifier
                    .padding(Spacing.xxxs)
                    .size(iconSize)
                    .clip(RoundedCornerShape(iconSize / 2))
                    .onClick(
                        onClick = rememberCallback {
                            onOpenCommunity?.invoke(community)
                        },
                    ),
                quality = FilterQuality.Low,
                url = icon,
                autoload = autoLoadImages,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
            )
        } else {
            PlaceholderImage(
                modifier = Modifier.onClick(
                    onClick = rememberCallback {
                        onOpenCommunity?.invoke(community)
                    },
                ),
                size = iconSize,
                title = name,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.widthIn(max = 100.dp),
                text = name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = fullTextColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (host.isNotEmpty()) {
                Text(
                    modifier = Modifier.widthIn(max = 100.dp),
                    text = host,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = ancillaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}