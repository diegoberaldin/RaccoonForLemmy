package com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings

sealed interface AppIconVariant {
    data object Default : AppIconVariant

    data object Alt1 : AppIconVariant

    data object Alt2 : AppIconVariant
}

interface AppIconManager {
    val supportsMultipleIcons: Boolean

    fun changeIcon(variant: AppIconVariant)
}

@Composable
fun AppIconVariant.toReadableName(): String =
    when (this) {
        AppIconVariant.Alt2 -> LocalStrings.current.appIconClassical
        AppIconVariant.Alt1 -> LocalStrings.current.appIconAlt1
        AppIconVariant.Default -> LocalStrings.current.appIconDefault
    }

fun AppIconVariant.toInt(): Int =
    when (this) {
        AppIconVariant.Alt2 -> 2
        AppIconVariant.Alt1 -> 1
        AppIconVariant.Default -> 0
    }

fun Int.toAppIconVariant(): AppIconVariant =
    when (this) {
        2 -> AppIconVariant.Alt2
        1 -> AppIconVariant.Alt1
        else -> AppIconVariant.Default
    }
