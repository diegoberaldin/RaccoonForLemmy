package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable

enum class UiFontFamily {
    TitilliumWeb,
    Dosis,
    Quattrocento,
    NotoSans,
    Prociono,
    ComicNeue,
    Poppins,
}

fun Int.toUiFontFamily() = when (this) {
    6 -> UiFontFamily.Poppins
    5 -> UiFontFamily.ComicNeue
    4 -> UiFontFamily.Prociono
    3 -> UiFontFamily.NotoSans
    2 -> UiFontFamily.Quattrocento
    1 -> UiFontFamily.Dosis
    else -> UiFontFamily.TitilliumWeb
}

fun UiFontFamily.toInt() = when (this) {
    UiFontFamily.Poppins -> 6
    UiFontFamily.ComicNeue -> 5
    UiFontFamily.Prociono -> 4
    UiFontFamily.NotoSans -> 3
    UiFontFamily.Quattrocento -> 2
    UiFontFamily.Dosis -> 1
    else -> 0
}

@Composable
fun UiFontFamily.toReadableName() = when (this) {
    UiFontFamily.Poppins -> "Poppins"
    UiFontFamily.ComicNeue -> "Comic Neue"
    UiFontFamily.Prociono -> "Prociono"
    UiFontFamily.NotoSans -> "Noto Sans"
    UiFontFamily.Quattrocento -> "Quattrocento"
    UiFontFamily.Dosis -> "Dosis"
    else -> "Titillium Web"
}
