package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing

@Composable
fun IndicatorCallout(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.padding(Spacing.s),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = FontFamily.Default,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
