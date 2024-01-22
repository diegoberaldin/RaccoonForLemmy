package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface UiBarTheme {
    data object Solid : UiBarTheme
    data object Transparent : UiBarTheme
    data object Opaque : UiBarTheme
}

fun UiBarTheme?.toInt(): Int = when (this) {
    UiBarTheme.Transparent -> 2
    UiBarTheme.Opaque -> 1
    else -> 0
}

@Composable
fun UiBarTheme?.toReadableName(): String = when (this) {
    UiBarTheme.Transparent -> stringResource(MR.strings.bar_theme_transparent)
    UiBarTheme.Opaque -> stringResource(MR.strings.bar_theme_opaque)
    else -> ""
}