package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick

@Composable
fun SettingsImageInfo(
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillBounds,
    title: String = "",
    url: String = "",
    onEdit: (() -> Unit)? = null,
) {
    val ancillaryColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    vertical = Spacing.xs,
                    horizontal = Spacing.m,
                ).onClick(
                    onClick = {
                        onEdit?.invoke()
                    },
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = ancillaryColor,
            )

            if (url.isNotEmpty()) {
                CustomImage(
                    modifier = imageModifier,
                    url = url,
                    quality = FilterQuality.Low,
                    contentScale = contentScale,
                )
            } else {
                Box(
                    modifier = imageModifier,
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}
