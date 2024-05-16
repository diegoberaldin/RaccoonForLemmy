package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.resources.di.getCoreResources

/*
 * Reference:
 * https://m3.material.io/styles/typography/type-scale-tokens#40fc37f8-3269-4aa3-9f28-c6113079ac5d
 */
@Composable
fun UiFontFamily.toTypography(): Typography {
    val coreResources = remember { getCoreResources() }
    val fontFamily =
        when (this) {
            UiFontFamily.NotoSans -> coreResources.notoSans
            UiFontFamily.CharisSIL -> coreResources.charisSil
            UiFontFamily.Poppins -> coreResources.poppins
            else -> FontFamily.Default
        }
    return Typography(
        // h1
        displayLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                letterSpacing = (-.25).sp,
                lineHeight = 64.sp,
            ),
        // h2
        displayMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                letterSpacing = 0.sp,
                lineHeight = 52.sp,
            ),
        // h3
        displaySmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                letterSpacing = 0.sp,
                lineHeight = 44.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                letterSpacing = 0.sp,
                lineHeight = 40.sp,
            ),
        // h4
        headlineMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                letterSpacing = 0.sp,
                lineHeight = 36.sp,
            ),
        // h5
        headlineSmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                letterSpacing = 0.sp,
                lineHeight = 23.sp,
            ),
        // h6
        titleLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                letterSpacing = 0.sp,
                lineHeight = 28.sp,
            ),
        // subtitle1
        titleMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                letterSpacing = (0.15).sp,
                lineHeight = 24.sp,
            ),
        // subtitle2
        titleSmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = (0.1).sp,
                lineHeight = 20.sp,
            ),
        // body1
        bodyLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                letterSpacing = (0.5).sp,
                lineHeight = 24.sp,
            ),
        // body2
        bodyMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                letterSpacing = (0.25).sp,
                lineHeight = 20.sp,
            ),
        // caption
        bodySmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                letterSpacing = (0.4).sp,
                lineHeight = 16.sp,
            ),
        // button
        labelLarge =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = (0.1).sp,
                lineHeight = 20.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = (0.5).sp,
                lineHeight = 16.sp,
            ),
        // overline
        labelSmall =
            TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = (0.5).sp,
                lineHeight = 16.sp,
            ),
    )
}
