package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha

@Composable
fun BottomSheetHeader(
    title: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BottomSheetHandle(
            modifier = Modifier.padding(bottom = Spacing.xs),
        )
        Text(
            modifier =
                Modifier.padding(
                    top = Spacing.xs,
                    bottom = Spacing.xs,
                ),
            text = title,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun BottomSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier.width(60.dp)
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
