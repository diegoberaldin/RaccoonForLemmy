package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

enum class UiFontFamily {
    Default,
    NotoSans,
    CharisSIL,
    Poppins,
    Comfortaa,
}

fun Int.toUiFontFamily() = when (this) {
    0 -> UiFontFamily.Poppins
    3 -> UiFontFamily.NotoSans
    4 -> UiFontFamily.CharisSIL
    5 -> UiFontFamily.Comfortaa
    else -> UiFontFamily.Default
}

fun UiFontFamily.toInt() = when (this) {
    UiFontFamily.Poppins -> 0
    UiFontFamily.NotoSans -> 3
    UiFontFamily.CharisSIL -> 4
    UiFontFamily.Comfortaa -> 5
    UiFontFamily.Default -> 7
}

@Composable
fun UiFontFamily.toReadableName() = when (this) {
    UiFontFamily.Poppins -> "Poppins"
    UiFontFamily.NotoSans -> "Noto Sans"
    UiFontFamily.CharisSIL -> "Charis SIL"
    UiFontFamily.Comfortaa -> "Comfortaa"
    UiFontFamily.Default -> stringResource(MR.strings.settings_font_family_default)
}
