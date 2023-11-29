package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable

enum class UiFontFamily {
    TitilliumWeb,
    Dosis,
    EBGaramond,
    NotoSans,
    Prociono,
    ComicNeue,
    Poppins,
}

fun Int.toUiFontFamily() = when (this) {
    6 -> UiFontFamily.TitilliumWeb
    5 -> UiFontFamily.ComicNeue
    4 -> UiFontFamily.Prociono
    3 -> UiFontFamily.NotoSans
    2 -> UiFontFamily.EBGaramond
    1 -> UiFontFamily.Dosis
    else -> UiFontFamily.Poppins
}

fun UiFontFamily.toInt() = when (this) {
    UiFontFamily.Poppins -> 0
    UiFontFamily.ComicNeue -> 5
    UiFontFamily.Prociono -> 4
    UiFontFamily.NotoSans -> 3
    UiFontFamily.EBGaramond -> 2
    UiFontFamily.Dosis -> 1
    UiFontFamily.TitilliumWeb -> 6
}

@Composable
fun UiFontFamily.toReadableName() = when (this) {
    UiFontFamily.Poppins -> "Poppins"
    UiFontFamily.ComicNeue -> "Comic Neue"
    UiFontFamily.Prociono -> "Prociono"
    UiFontFamily.NotoSans -> "Noto Sans"
    UiFontFamily.EBGaramond -> "EB Garamond"
    UiFontFamily.Dosis -> "Dosis"
    else -> "Titillium Web"
}
