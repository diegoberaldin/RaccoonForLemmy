package com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.TextToolbar

@Composable
expect fun getCustomTextToolbar(
    onShare: () -> Unit,
    onQuote: () -> Unit,
): TextToolbar
