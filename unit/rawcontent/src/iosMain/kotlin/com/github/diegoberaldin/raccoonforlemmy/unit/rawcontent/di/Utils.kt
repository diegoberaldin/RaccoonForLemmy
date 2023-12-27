package com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar

@Composable
actual fun getCustomTextToolbar(
    onShare: () -> Unit,
    onQuote: () -> Unit,
): TextToolbar {
    return LocalTextToolbar.current
}
