package com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.CustomTextToolbar

@Composable
actual fun getCustomTextToolbar(
    onShare: () -> Unit,
    onQuote: () -> Unit,
): TextToolbar {
    return CustomTextToolbar(
        view = LocalView.current,
        onShare = onShare,
        onQuote = onQuote,
    )
}
