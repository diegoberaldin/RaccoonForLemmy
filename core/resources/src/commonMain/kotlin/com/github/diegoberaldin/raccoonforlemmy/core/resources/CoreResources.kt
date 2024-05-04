package com.github.diegoberaldin.raccoonforlemmy.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily

interface CoreResources {
    val github: Painter @Composable get
    val lemmy: Painter @Composable get
    val appIconDefault: Painter @Composable get
    val appIconAlt1: Painter @Composable get
    val notoSans: FontFamily @Composable get
    val poppins: FontFamily @Composable get
    val charisSil: FontFamily @Composable get
}
