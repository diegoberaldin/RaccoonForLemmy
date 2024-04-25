package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha

@Composable
fun BottomSheetHandle(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(60.dp)
            .padding(
                top = Spacing.s,
                bottom = Spacing.xxxs,
            )
            .height(3.dp)
            .background(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = ancillaryTextAlpha),
                shape = RoundedCornerShape(1.5.dp),
            ),
    )
}
