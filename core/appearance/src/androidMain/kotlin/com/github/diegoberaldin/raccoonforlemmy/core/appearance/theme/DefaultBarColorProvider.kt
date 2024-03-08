package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme

class DefaultBarColorProvider : BarColorProvider {
    @Composable
    override fun setBarColorAccordingToTheme(theme: UiTheme, barTheme: UiBarTheme) {
        val view = LocalView.current
        LaunchedEffect(theme, barTheme) {
            (view.context as? Activity)?.window?.apply {
                val baseColor = when (theme) {
                    UiTheme.Light -> Color.White
                    else -> Color.Black
                }
                val barColor = when (barTheme) {
                    UiBarTheme.Opaque -> baseColor.copy(alpha = 0.25f)
                    UiBarTheme.Transparent -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Color.Transparent
                    } else {
                        baseColor
                    }

                    else -> baseColor
                }.toArgb()
                statusBarColor = barColor
                navigationBarColor = barColor

                if (barTheme != UiBarTheme.Solid) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        setDecorFitsSystemWindows(false)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        isStatusBarContrastEnforced = true
                        isNavigationBarContrastEnforced = true
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
