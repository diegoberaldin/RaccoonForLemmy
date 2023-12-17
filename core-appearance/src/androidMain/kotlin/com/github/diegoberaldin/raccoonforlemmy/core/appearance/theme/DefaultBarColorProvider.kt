package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme

class DefaultBarColorProvider : BarColorProvider {
    @Composable
    override fun setBarColorAccordingToTheme(theme: UiTheme, transparent: Boolean) {
        val view = LocalView.current
        LaunchedEffect(theme, transparent) {
            (view.context as? Activity)?.window?.apply {
                statusBarColor = when {
                    transparent -> Color.Transparent
                    theme == UiTheme.Light -> Color.White
                    else -> Color.Black
                }.toArgb()
                navigationBarColor = when {
                    transparent -> Color.Transparent
                    theme == UiTheme.Light -> Color.White
                    else -> Color.Black
                }.toArgb()

                if (transparent) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        setDecorFitsSystemWindows(false)
                    }
                }

                WindowCompat.getInsetsController(this, decorView).apply {
                    isAppearanceLightStatusBars = when (theme) {
                        UiTheme.Light -> true
                        else -> false
                    }
                    isAppearanceLightNavigationBars = when (theme) {
                        UiTheme.Light -> true
                        else -> false
                    }
                }
            }
        }
    }
}
