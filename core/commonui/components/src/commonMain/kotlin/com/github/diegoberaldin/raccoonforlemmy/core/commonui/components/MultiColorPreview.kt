package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MultiColorPreview(
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier,
    ) {
        val step = 360f / colors.size.coerceAtLeast(1)
        var start = -90f
        for (i in colors.indices) {
            drawArc(
                color = colors[i],
                startAngle = start,
                sweepAngle = step,
                useCenter = true,
            )
            start += step
        }
    }
}
