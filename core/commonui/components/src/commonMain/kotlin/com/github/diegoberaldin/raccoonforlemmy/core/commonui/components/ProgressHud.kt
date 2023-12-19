package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ProgressHud(
    overlayColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlayColor),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = color,
        )
    }
}