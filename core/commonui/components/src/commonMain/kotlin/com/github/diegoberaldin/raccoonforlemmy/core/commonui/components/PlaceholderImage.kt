package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing

@Composable
fun PlaceholderImage(
    modifier: Modifier = Modifier,
    size: Dp,
    title: String,
) {
    Box(
        modifier = modifier
            .padding(Spacing.xxxs)
            .size(size)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(size / 2),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title.firstOrNull()?.toString().orEmpty().uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
