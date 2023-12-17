package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.di

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
