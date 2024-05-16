package com.github.diegoberaldin.raccoonforlemmy.core.utils.compose

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

@Composable
fun Modifier.shimmerEffect(duration: Int = 1000): Modifier {
    val c1 = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    val c2 = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
    val colors = listOf(c1, c2, c1)
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(duration),
            ),
    )

    return this then
        background(
            brush =
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(startOffsetX, 0f),
                    end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat()),
                ),
        ).alpha(0.5f).onGloballyPositioned {
            size = it.size
        }
}
