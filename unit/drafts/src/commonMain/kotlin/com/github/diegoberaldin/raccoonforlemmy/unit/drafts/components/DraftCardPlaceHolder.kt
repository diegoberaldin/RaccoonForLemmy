package com.github.diegoberaldin.raccoonforlemmy.unit.drafts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.shimmerEffect

@Composable
fun DraftCardPlaceHolder(postLayout: PostLayout) {
    Column(
        modifier =
            Modifier.then(
                if (postLayout == PostLayout.Card) {
                    Modifier
                        .padding(horizontal = Spacing.xs)
                        .shadow(elevation = 5.dp, shape = RoundedCornerShape(CornerSize.l))
                        .clip(RoundedCornerShape(CornerSize.l))
                        .padding(horizontal = Spacing.xs)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                        )
                        .padding(Spacing.s)
                } else {
                    Modifier
                },
            ),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(
            modifier =
                Modifier
                    .padding(vertical = Spacing.xxxs)
                    .height(14.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerSize.m))
                    .shimmerEffect(),
        )
        Box(
            modifier =
                Modifier
                    .padding(vertical = Spacing.xxxs)
                    .height(50.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerSize.m))
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
