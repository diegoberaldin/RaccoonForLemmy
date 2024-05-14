package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.materialkolor.dynamicColorScheme

internal class DefaultColorSchemeProvider(private val context: Context) : ColorSchemeProvider {

    override val supportsDynamicColors: Boolean
        @ChecksSdkIntAtLeast(31)
        get() {
            return Build.VERSION.SDK_INT >= 31
        }

    @SuppressLint("NewApi")
    override fun getColorScheme(
        theme: UiTheme,
        dynamic: Boolean,
        customSeed: Color?,
    ): ColorScheme = when (theme) {
        UiTheme.Dark -> {
            when {
                dynamic -> {
                    dynamicDarkColorScheme(context)
                }

                customSeed != null -> {
                    dynamicColorScheme(customSeed, true)
                }

                else -> {
                    DarkColors
                }
            }
        }

        UiTheme.Black -> {
            when {
                dynamic -> {
                    dynamicDarkColorScheme(context).blackify()
                }

                customSeed != null -> {
                    dynamicColorScheme(customSeed, true).blackify()
                }

                else -> {
                    BlackColors
                }
            }
        }

        else -> {
            when {
                dynamic -> {
                    dynamicLightColorScheme(context)
                }

                customSeed != null -> {
                    dynamicColorScheme(customSeed, false)
                }

                else -> {
                    LightColors
                }
            }
        }
    }
}
