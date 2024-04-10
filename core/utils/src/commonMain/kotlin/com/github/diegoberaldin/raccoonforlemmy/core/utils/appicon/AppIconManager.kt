package com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon

import androidx.compose.runtime.Composable

sealed interface AppIconVariant {
    data object Default : AppIconVariant
    data object Alt1 : AppIconVariant
}

interface AppIconManager {
    val supportsMultipleIcons: Boolean
    fun changeIcon(variant: AppIconVariant)
}

@Composable
fun AppIconVariant.toReadableName(): String = when (this) {
    // TODO: l10n
    AppIconVariant.Alt1 -> "Monochrome"
    AppIconVariant.Default -> "Default"
}

fun AppIconVariant.toInt(): Int = when (this) {
    AppIconVariant.Alt1 -> 1
    AppIconVariant.Default -> 0
}

fun Int.toAppIconVariant(): AppIconVariant = when (this) {
    1 -> AppIconVariant.Alt1
    else -> AppIconVariant.Default
}