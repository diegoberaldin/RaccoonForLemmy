package com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.fontFamilyResource

@Composable
internal fun getTypography(
    fontFamily: UiFontFamily = UiFontFamily.TitilliumWeb,
): Typography {
    val fontFamily = when (fontFamily) {
        UiFontFamily.Prociono -> fontFamilyResource(MR.fonts.Prociono.regular)
        UiFontFamily.NotoSans -> fontFamilyResource(MR.fonts.NotoSans.regular)
        UiFontFamily.EBGaramond -> fontFamilyResource(MR.fonts.EBGaramond.regular)
        UiFontFamily.Dosis -> fontFamilyResource(MR.fonts.Dosis.regular)
        UiFontFamily.ComicNeue -> fontFamilyResource(MR.fonts.ComicNeue.regular)
        UiFontFamily.Poppins -> fontFamilyResource(MR.fonts.Poppins.regular)
        else -> fontFamilyResource(MR.fonts.TitilliumWeb.regular)
    }
    return Typography(
        // h1
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 96.sp,
            letterSpacing = (-1.5).sp,
        ),
        // h2
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 60.sp,
            letterSpacing = (-0.5).sp,
        ),
        // h3
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 38.sp,
            letterSpacing = 0.sp,
        ),
        // h4
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            letterSpacing = (0.25).sp,
        ),
        // h5
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            letterSpacing = 0.sp,
        ),
        // h6
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            letterSpacing = (0.15).sp,
        ),
        // subtitle1
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = (0.15).sp,
        ),
        // subtitle2
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = (0.1).sp,
        ),
        // body1
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = (0.5).sp,
        ),
        // body2
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            letterSpacing = (0.25).sp,
        ),
        // button
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = (0.15).sp, // original: 1.25
        ),
        // caption
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = (0.4).sp,
        ),
        // overline
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            letterSpacing = (0.5).sp, // original: 1.5
        ),
    )
}
