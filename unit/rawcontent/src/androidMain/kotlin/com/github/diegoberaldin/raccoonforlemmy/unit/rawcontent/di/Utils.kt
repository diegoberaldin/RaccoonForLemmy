package com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.CustomTextToolbar

@Composable
actual fun getCustomTextToolbar(
    isLogged: Boolean,
    onShare: () -> Unit,
    onQuote: () -> Unit,
): TextToolbar {
    return CustomTextToolbar(
        view = LocalView.current,
        isLogged = isLogged,
        onShare = onShare,
        onQuote = onQuote,
    )
}
