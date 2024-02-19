package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getBarColorProvider
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository

@Composable
fun AppTheme(
    useDynamicColors: Boolean,
    barTheme: UiBarTheme = UiBarTheme.Solid,
    content: @Composable () -> Unit,
) {
    val repository = remember {
        getThemeRepository()
    }

    val themeState by repository.uiTheme.collectAsState()
    val customSeedColor by repository.customSeedColor.collectAsState()
    val defaultTheme = if (isSystemInDarkTheme()) {
        UiTheme.Dark
    } else {
        UiTheme.Light
    }

    val colorSchemeProvider = remember { getColorSchemeProvider() }
    val colorScheme = colorSchemeProvider.getColorScheme(
        theme = themeState ?: defaultTheme,
        dynamic = useDynamicColors,
        customSeed = customSeedColor,
    )

    val fontFamily by repository.uiFontFamily.collectAsState()
    val typography = fontFamily.toTypography()

    val barColorProvider = remember { getBarColorProvider() }
    barColorProvider.setBarColorAccordingToTheme(
        theme = themeState ?: defaultTheme,
        barTheme = barTheme,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content,
    )
}
