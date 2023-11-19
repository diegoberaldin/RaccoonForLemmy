package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable

enum class UiFontFamily {
    TitilliumWeb,
    Dosis,
    CormorantGaramond,
    NotoSans,
    Prociono,
    Laila,
    Poppins,
}

fun Int.toUiFontFamily() = when (this) {
    6 -> UiFontFamily.Poppins
    5 -> UiFontFamily.Laila
    4 -> UiFontFamily.Prociono
    3 -> UiFontFamily.NotoSans
    2 -> UiFontFamily.CormorantGaramond
    1 -> UiFontFamily.Dosis
    else -> UiFontFamily.TitilliumWeb
}

fun UiFontFamily.toInt() = when (this) {
    UiFontFamily.Poppins -> 6
    UiFontFamily.Laila -> 5
    UiFontFamily.Prociono -> 4
    UiFontFamily.NotoSans -> 3
    UiFontFamily.CormorantGaramond -> 2
    UiFontFamily.Dosis -> 1
    else -> 0
}

@Composable
fun UiFontFamily.toReadableName() = when (this) {
    UiFontFamily.Poppins -> "Poppins"
    UiFontFamily.Laila -> "Laila"
    UiFontFamily.Prociono -> "Prociono"
    UiFontFamily.NotoSans -> "Noto Sans"
    UiFontFamily.CormorantGaramond -> "Cormorant Garamond"
    UiFontFamily.Dosis -> "Dosis"
    else -> "Titillium Web"
}
