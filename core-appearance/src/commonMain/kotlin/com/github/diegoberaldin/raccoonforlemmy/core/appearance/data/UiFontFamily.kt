package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable

sealed interface UiFontFamily {
    data object TitilliumWeb : UiFontFamily
    data object Dosis : UiFontFamily
    data object CormorantGaramond : UiFontFamily
    data object NotoSans : UiFontFamily
    data object Prociono : UiFontFamily
    data object Laila : UiFontFamily
}

fun Int.toUiFontFamily() = when (this) {
    5 -> UiFontFamily.Laila
    4 -> UiFontFamily.Prociono
    3 -> UiFontFamily.NotoSans
    2 -> UiFontFamily.CormorantGaramond
    1 -> UiFontFamily.Dosis
    else -> UiFontFamily.TitilliumWeb
}

fun UiFontFamily.toInt() = when (this) {
    UiFontFamily.Laila -> 5
    UiFontFamily.Prociono -> 4
    UiFontFamily.NotoSans -> 3
    UiFontFamily.CormorantGaramond -> 2
    UiFontFamily.Dosis -> 1
    else -> 0
}

@Composable
fun UiFontFamily.toReadableName() = when (this) {
    UiFontFamily.Laila -> "Laila"
    UiFontFamily.Prociono -> "Prociono"
    UiFontFamily.NotoSans -> "Noto Sans"
    UiFontFamily.CormorantGaramond -> "Cormorant Garamond"
    UiFontFamily.Dosis -> "Dosis"
    else -> "Titillium Web"
}
