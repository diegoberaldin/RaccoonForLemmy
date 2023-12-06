package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface UiTheme {
    data object Light : UiTheme
    data object Dark : UiTheme
    data object Black : UiTheme
}

fun Int?.toUiTheme(): UiTheme? = when (this) {
    2 -> UiTheme.Black
    1 -> UiTheme.Dark
    0 -> UiTheme.Light
    else -> null
}

fun UiTheme?.toInt(): Int? = when (this) {
    UiTheme.Black -> 2
    UiTheme.Dark -> 1
    UiTheme.Light -> 0
    else -> null
}

@Composable
fun UiTheme?.toReadableName() = when (this) {
    UiTheme.Black -> stringResource(MR.strings.settings_theme_black)
    UiTheme.Dark -> stringResource(MR.strings.settings_theme_dark)
    UiTheme.Light -> stringResource(MR.strings.settings_theme_light)
    else -> stringResource(MR.strings.settings_font_family_default)
}

fun UiTheme.toIcon() = when (this) {
    UiTheme.Black -> Icons.Default.DarkMode
    UiTheme.Dark -> Icons.Outlined.DarkMode
    UiTheme.Light -> Icons.Default.LightMode
}
