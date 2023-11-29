package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable

enum class UiFontFamily {
    TitilliumWeb,
    Dosis,
    EBGaramond,
    NotoSans,
    CharisSIL,
    ComicNeue,
    Poppins,
}

fun Int.toUiFontFamily() = when (this) {
    6 -> UiFontFamily.TitilliumWeb
    5 -> UiFontFamily.ComicNeue
    4 -> UiFontFamily.CharisSIL
    3 -> UiFontFamily.NotoSans
    2 -> UiFontFamily.EBGaramond
    1 -> UiFontFamily.Dosis
    else -> UiFontFamily.Poppins
}

fun UiFontFamily.toInt() = when (this) {
    UiFontFamily.Poppins -> 0
    UiFontFamily.ComicNeue -> 5
    UiFontFamily.CharisSIL -> 4
    UiFontFamily.NotoSans -> 3
    UiFontFamily.EBGaramond -> 2
    UiFontFamily.Dosis -> 1
    UiFontFamily.TitilliumWeb -> 6
}

@Composable
fun UiFontFamily.toReadableName() = when (this) {
    UiFontFamily.Poppins -> "Poppins"
    UiFontFamily.ComicNeue -> "Comic Neue"
    UiFontFamily.CharisSIL -> "Charis SIL"
    UiFontFamily.NotoSans -> "Noto Sans"
    UiFontFamily.EBGaramond -> "EB Garamond"
    UiFontFamily.Dosis -> "Dosis"
    else -> "Titillium Web"
}
