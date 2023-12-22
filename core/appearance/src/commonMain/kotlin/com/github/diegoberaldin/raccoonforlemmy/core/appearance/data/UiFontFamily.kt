package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

enum class UiFontFamily {
    TitilliumWeb,
    NotoSans,
    CharisSIL,
    Comfortaa,
    Poppins,
    Default,
}

fun Int.toUiFontFamily() = when (this) {
    7 -> UiFontFamily.Default
    6 -> UiFontFamily.TitilliumWeb
    5 -> UiFontFamily.Comfortaa
    4 -> UiFontFamily.CharisSIL
    3 -> UiFontFamily.NotoSans
    else -> UiFontFamily.Poppins
}

fun UiFontFamily.toInt() = when (this) {
    UiFontFamily.Poppins -> 0
    UiFontFamily.Comfortaa -> 5
    UiFontFamily.CharisSIL -> 4
    UiFontFamily.NotoSans -> 3
    UiFontFamily.TitilliumWeb -> 6
    UiFontFamily.Default -> 7
}

@Composable
fun UiFontFamily.toReadableName() = when (this) {
    UiFontFamily.Default -> stringResource(MR.strings.settings_font_family_default)
    UiFontFamily.Poppins -> "Poppins"
    UiFontFamily.Comfortaa -> "Comfortaa"
    UiFontFamily.CharisSIL -> "Charis SIL"
    UiFontFamily.NotoSans -> "Noto Sans"
    else -> "Titillium Web"
}
