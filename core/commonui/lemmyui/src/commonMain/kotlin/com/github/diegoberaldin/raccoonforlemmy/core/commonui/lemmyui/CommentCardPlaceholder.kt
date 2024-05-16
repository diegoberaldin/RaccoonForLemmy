package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.shimmerEffect

@Composable
fun CommentCardPlaceholder(
    modifier: Modifier = Modifier,
    hideAuthor: Boolean = false,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        if (!hideAuthor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Box(
                    modifier =
                        Modifier.size(IconSize.s)
                            .clip(CircleShape)
                            .shimmerEffect(),
                )
                Column(
                    modifier = Modifier.padding(vertical = Spacing.xxxs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    Box(
                        modifier =
                            Modifier.height(IconSize.s)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(CornerSize.m))
                                .shimmerEffect(),
                    )
                    Box(
                        modifier =
                            Modifier.height(IconSize.s)
                                .fillMaxWidth(0.5f)
                                .clip(RoundedCornerShape(CornerSize.m))
                                .shimmerEffect(),
                    )
                }
            }
        }
        Box(
            modifier =
                Modifier
                    .height(80.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerSize.s))
                    .shimmerEffect(),
        )
        Box(
            modifier =
                Modifier
                    .height(IconSize.l)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerSize.m))
                    .shimmerEffect(),
        )
    }
}
