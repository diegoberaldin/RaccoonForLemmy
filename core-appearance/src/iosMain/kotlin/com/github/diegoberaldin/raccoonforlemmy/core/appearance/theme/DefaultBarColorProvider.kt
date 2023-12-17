package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleBlackTranslucent
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.setStatusBarStyle

class DefaultBarColorProvider : BarColorProvider {
    @Composable
    override fun setBarColorAccordingToTheme(theme: UiTheme, transparent: Boolean) {
        LaunchedEffect(theme) {
            val style = when {
                transparent -> UIStatusBarStyleBlackTranslucent
                theme == UiTheme.Light -> UIStatusBarStyleLightContent
                else -> UIStatusBarStyleDarkContent
            }
            UIApplication.sharedApplication().setStatusBarStyle(style)
        }
    }
}
