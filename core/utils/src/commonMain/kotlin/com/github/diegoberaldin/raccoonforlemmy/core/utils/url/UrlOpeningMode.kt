package com.github.diegoberaldin.raccoonforlemmy.core.utils.url

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings

sealed interface UrlOpeningMode {
    data object CustomTabs : UrlOpeningMode

    data object External : UrlOpeningMode

    data object Internal : UrlOpeningMode
}

fun UrlOpeningMode.toInt(): Int =
    when (this) {
        UrlOpeningMode.CustomTabs -> 2
        UrlOpeningMode.External -> 1
        else -> 0
    }

fun Int.toUrlOpeningMode(): UrlOpeningMode =
    when (this) {
        2 -> UrlOpeningMode.CustomTabs
        1 -> UrlOpeningMode.External
        else -> UrlOpeningMode.Internal
    }

@Composable
fun UrlOpeningMode.toReadableName(): String =
    when (this) {
        UrlOpeningMode.CustomTabs -> LocalStrings.current.settingsUrlOpeningModeCustomTabs
        UrlOpeningMode.External -> LocalStrings.current.settingsUrlOpeningModeExternal
        UrlOpeningMode.Internal -> LocalStrings.current.settingsUrlOpeningModeInternal
    }
