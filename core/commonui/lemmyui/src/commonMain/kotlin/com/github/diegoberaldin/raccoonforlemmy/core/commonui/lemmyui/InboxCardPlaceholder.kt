package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

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
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.shimmerEffect

@Composable
fun InboxCardPlaceholder(
    postLayout: PostLayout = PostLayout.Card,
) {
    Column(
        modifier = Modifier.let {
            if (postLayout == PostLayout.Card) {
                it.padding(horizontal = Spacing.xs).background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                    shape = RoundedCornerShape(CornerSize.l),
                ).padding(Spacing.s)
            } else {
                it
            }
        },
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .height(IconSize.l)
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerSize.m))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .padding(vertical = Spacing.xxxs)
                .height(50.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerSize.m))
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .height(IconSize.l)
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerSize.m))
                .shimmerEffect()
        )
    }
}
