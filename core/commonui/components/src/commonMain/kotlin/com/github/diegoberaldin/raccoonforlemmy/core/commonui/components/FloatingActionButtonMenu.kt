package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback

data class FloatingActionButtonMenuItem(
    val icon: ImageVector,
    val text: String,
    val onSelected: (() -> Unit)? = null,
)

private const val ANIMATION_DURATION = 150

@Composable
fun FloatingActionButtonMenu(
    modifier: Modifier = Modifier,
    items: List<FloatingActionButtonMenuItem> = emptyList(),
) {
    val themeRepository = remember { getThemeRepository() }
    val schemeProvider = remember { getColorSchemeProvider() }
    val seedColor by themeRepository.customSeedColor.collectAsState()
    val theme by themeRepository.uiTheme.collectAsState()
    val dynamicColors by themeRepository.dynamicColors.collectAsState()
    var fabExpanded by remember { mutableStateOf(false) }
    val fabRotation by animateFloatAsState(if (fabExpanded) 45f else 0f)
    val enterTransition = remember {
        fadeIn(
            initialAlpha = 0.3f,
            animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
        )
    }
    val exitTransition = remember {
        fadeOut(
            animationSpec = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing)
        )
    }
    val numberOfItems by animateIntAsState(
        targetValue = if (fabExpanded) items.size else 0,
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION * items.size,
            easing = LinearEasing,
        )
    )
    val indices: List<Int> = if (numberOfItems == 0) {
        emptyList()
    } else {
        buildList {
            for (i in 0 until numberOfItems) {
                add(items.size - i - 1)
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            horizontalAlignment = Alignment.End,
        ) {
            Spacer(modifier = Modifier.height(Spacing.m))
            items.forEachIndexed { idx, item ->
                AnimatedVisibility(
                    visible = idx in indices,
                    enter = enterTransition,
                    exit = exitTransition
                ) {
                    Row(
                        modifier = Modifier.onClick(
                            onClick = rememberCallback {
                                fabExpanded = false
                                item.onSelected?.invoke()
                            },
                        ).padding(end = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                    ) {
                        Text(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(CornerSize.s),
                                ).padding(
                                    vertical = Spacing.s,
                                    horizontal = Spacing.s
                                ),
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Icon(
                            modifier = Modifier
                                .size(IconSize.m)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ).padding(6.dp),
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xxs))
        }


        val fabContainerColor = when (theme) {
            UiTheme.Black -> schemeProvider.getColorScheme(
                theme = UiTheme.Dark,
                dynamic = dynamicColors,
                customSeed = seedColor
            ).primaryContainer

            else -> MaterialTheme.colorScheme.primaryContainer
        }
        FloatingActionButton(
            containerColor = fabContainerColor,
            shape = CircleShape,
            onClick = {
                fabExpanded = !fabExpanded
            },
            content = {
                Icon(
                    modifier = Modifier.rotate(fabRotation),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            },
        )
    }
}
