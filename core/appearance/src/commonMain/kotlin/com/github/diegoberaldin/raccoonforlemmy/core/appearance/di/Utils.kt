package com.github.diegoberaldin.raccoonforlemmy.core.appearance.di

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.BarColorProvider
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ColorSchemeProvider
import org.koin.core.module.Module

expect val nativeAppearanceModule: Module
expect fun getThemeRepository(): ThemeRepository
expect fun getColorSchemeProvider(): ColorSchemeProvider
expect fun getBarColorProvider(): BarColorProvider
