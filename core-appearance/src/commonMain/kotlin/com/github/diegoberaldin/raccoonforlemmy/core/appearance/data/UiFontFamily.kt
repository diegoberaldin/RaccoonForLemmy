package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

enum class UiFontFamily {
    TitilliumWeb,
    Dosis,
    EBGaramond,
    NotoSans,
    CharisSIL,
    AveriaSansLibre,
    Poppins,
    Default,
}

fun Int.toUiFontFamily() = when (this) {
    7 -> UiFontFamily.Default
    6 -> UiFontFamily.TitilliumWeb
    5 -> UiFontFamily.AveriaSansLibre
    4 -> UiFontFamily.CharisSIL
    3 -> UiFontFamily.NotoSans
    2 -> UiFontFamily.EBGaramond
    1 -> UiFontFamily.Dosis
    else -> UiFontFamily.Poppins
}

fun UiFontFamily.toInt() = when (this) {
    UiFontFamily.Poppins -> 0
    UiFontFamily.AveriaSansLibre -> 5
    UiFontFamily.CharisSIL -> 4
    UiFontFamily.NotoSans -> 3
    UiFontFamily.EBGaramond -> 2
    UiFontFamily.Dosis -> 1
    UiFontFamily.TitilliumWeb -> 6
    UiFontFamily.Default -> 7
}

@Composable
fun UiFontFamily.toReadableName() = when (this) {
    UiFontFamily.Default -> stringResource(MR.strings.settings_font_family_default)
    UiFontFamily.Poppins -> "Poppins"
    UiFontFamily.AveriaSansLibre -> "Averia Sans Libre"
    UiFontFamily.CharisSIL -> "Charis SIL"
    UiFontFamily.NotoSans -> "Noto Sans"
    UiFontFamily.EBGaramond -> "EB Garamond"
    UiFontFamily.Dosis -> "Dosis"
    else -> "Titillium Web"
}
