package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.racconforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun UserHeader(
    user: UserModel,
    onOpenBookmarks: (() -> Unit)? = null,
) {
    val userAvatar = user.avatar.orEmpty()
    val userDisplayName = user.name
    val iconSize = 80.dp
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val banner = user.banner.orEmpty()
            if (banner.isNotEmpty()) {
                val painterResource = asyncPainterResource(banner)
                KamelImage(
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f),
                    resource = painterResource,
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f),
                )
            }
            Column(
                modifier = Modifier.graphicsLayer(translationY = -(iconSize / 2).toLocalPixel()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                if (userAvatar.isNotEmpty()) {
                    val painterResource =
                        asyncPainterResource(data = userAvatar)
                    KamelImage(
                        modifier = Modifier.padding(Spacing.xxxs).size(iconSize)
                            .clip(RoundedCornerShape(iconSize / 2)),
                        resource = painterResource,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                    )
                } else {
                    Box(
                        modifier = Modifier.padding(Spacing.xxxs)
                            .size(iconSize)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(iconSize / 2),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.toString()
                                .orEmpty()
                                .uppercase(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                Text(
                    text = buildString {
                        append(userDisplayName)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = user.host,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        if (onOpenBookmarks != null) {
            Icon(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.s)
                    .onClick {
                        onOpenBookmarks.invoke()
                    },
                imageVector = Icons.Outlined.Bookmarks,
                contentDescription = null,
            )
        }
    }
}
