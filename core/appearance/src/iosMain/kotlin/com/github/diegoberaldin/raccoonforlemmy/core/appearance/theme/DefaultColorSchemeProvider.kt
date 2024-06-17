package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.materialkolor.dynamicColorScheme

internal class DefaultColorSchemeProvider : ColorSchemeProvider {
    override val supportsDynamicColors = false

    override fun getColorScheme(
        theme: UiTheme,
        dynamic: Boolean,
        customSeed: Color?,
    ): ColorScheme =
        when (theme) {
            UiTheme.Dark -> {
                if (customSeed != null) {
                    dynamicColorScheme(
                        seedColor = customSeed,
                        isDark = true,
                        isAmoled = false,
                    )
                } else {
                    DarkColors
                }
            }

            UiTheme.Black -> {
                if (customSeed != null) {
                    dynamicColorScheme(
                        seedColor = customSeed,
                        isDark = true,
                        isAmoled = true,
                    )
                } else {
                    BlackColors
                }
            }

            else -> {
                if (customSeed != null) {
                    dynamicColorScheme(
                        seedColor = customSeed,
                        isDark = false,
                        isAmoled = false,
                    )
                } else {
                    LightColors
                }
            }
        }
}
