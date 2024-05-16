package com.github.diegoberaldin.raccoonforlemmy.unit.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.shimmerEffect

@Composable
internal fun MessageCardPlaceholder() {
    Box(
        modifier =
            Modifier
                .height(100.dp)
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs, vertical = Spacing.xs)
                .clip(RoundedCornerShape(CornerSize.s))
                .shimmerEffect(),
    )
}
