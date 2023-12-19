package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme

interface BarColorProvider {
    @Composable
    fun setBarColorAccordingToTheme(theme: UiTheme, transparent: Boolean)
}