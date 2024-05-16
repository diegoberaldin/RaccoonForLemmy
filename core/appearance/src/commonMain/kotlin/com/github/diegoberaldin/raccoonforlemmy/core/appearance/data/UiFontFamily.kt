package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

enum class UiFontFamily {
    Default,
    NotoSans,
    CharisSIL,
    Poppins,
}

fun Int.toUiFontFamily() =
    when (this) {
        0 -> UiFontFamily.Poppins
        3 -> UiFontFamily.NotoSans
        4 -> UiFontFamily.CharisSIL
        else -> UiFontFamily.Default
    }

fun UiFontFamily.toInt() =
    when (this) {
        UiFontFamily.Poppins -> 0
        UiFontFamily.NotoSans -> 3
        UiFontFamily.CharisSIL -> 4
        UiFontFamily.Default -> 7
    }

@Composable
fun UiFontFamily.toReadableName() =
    when (this) {
        UiFontFamily.Poppins -> "Poppins"
        UiFontFamily.NotoSans -> "Noto Sans"
        UiFontFamily.CharisSIL -> "Charis SIL"
        UiFontFamily.Default -> LocalXmlStrings.current.settingsFontFamilyDefault
    }
