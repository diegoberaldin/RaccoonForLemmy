package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ancillaryTextAlpha

@Composable
fun FeedbackButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    tintColor: Color = MaterialTheme.colorScheme.background.copy(alpha = ancillaryTextAlpha),
    onClick: () -> Unit,
) {
    var zoomed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (zoomed) 1.5f else 1f,
        animationSpec = spring(stiffness = StiffnessMediumLow),
    )
    Image(
        modifier =
            modifier
                .scale(scale)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            zoomed = true
                            tryAwaitRelease()
                            zoomed = false
                        },
                        onTap = {
                            onClick()
                        },
                    )
                },
        imageVector = imageVector,
        contentDescription = null,
        colorFilter =
            ColorFilter.tint(
                color = tintColor,
            ),
    )
}
