package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme

class DefaultBarColorProvider : BarColorProvider {
    @Composable
    override fun setBarColorAccordingToTheme(theme: UiTheme) {
        val view = LocalView.current
        LaunchedEffect(theme) {
            (view.context as? Activity)?.window?.apply {
                statusBarColor = when (theme) {
                    UiTheme.Light -> Color.White
                    else -> Color.Black
                }.toArgb()
                navigationBarColor = when (theme) {
                    UiTheme.Light -> Color.White
                    else -> Color.Black
                }.toArgb()
                WindowCompat.getInsetsController(this, view).isAppearanceLightStatusBars =
                    when (theme) {
                        UiTheme.Light -> true
                        else -> false
                    }
                WindowCompat.getInsetsController(this, view).isAppearanceLightNavigationBars =
                    when (theme) {
                        UiTheme.Light -> true
                        else -> false
                    }
            }
        }
    }
}
