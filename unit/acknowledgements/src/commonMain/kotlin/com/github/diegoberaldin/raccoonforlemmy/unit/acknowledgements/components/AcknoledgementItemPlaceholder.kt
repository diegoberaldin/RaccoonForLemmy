package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.components

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
fun AcknoledgementItemPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier.padding(
                vertical = Spacing.xs,
                horizontal = Spacing.s,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(
            modifier =
                Modifier
                    .size(IconSize.xl)
                    .clip(CircleShape)
                    .shimmerEffect(),
        )
        Column(
            modifier = Modifier.padding(start = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Box(
                modifier =
                    Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.s))
                        .shimmerEffect(),
            )
            Box(
                modifier =
                    Modifier
                        .height(14.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerSize.s))
                        .shimmerEffect(),
            )
        }
    }
}
