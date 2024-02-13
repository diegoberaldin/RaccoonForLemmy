package com.github.diegoberaldin.raccoonforlemmy.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
object CoreResources {

    val matrix: Painter
        @Composable
        get() = drawable("ic_matrix")

    val github: Painter
        @Composable
        get() = drawable("ic_github")

    val lemmy: Painter
        @Composable
        get() = drawable("ic_lemmy")

    val notoSans: FontFamily
        @Composable
        get() = FontFamily(
            font("NotoSans", "notosans_regular", FontWeight.Normal, FontStyle.Normal),
            font("NotoSans", "notosans_bold", FontWeight.Bold, FontStyle.Normal),
            font("NotoSans", "notosans_medium", FontWeight.Medium, FontStyle.Normal),
            font("NotoSans", "notosans_italic", FontWeight.Normal, FontStyle.Italic),
        )

    val poppins: FontFamily
        @Composable
        get() = FontFamily(
            font("Poppins", "poppins_regular", FontWeight.Normal, FontStyle.Normal),
            font("Poppins", "poppins_bold", FontWeight.Bold, FontStyle.Normal),
            font("Poppins", "poppins_medium", FontWeight.Medium, FontStyle.Normal),
            font("Poppins", "poppins_italic", FontWeight.Normal, FontStyle.Italic),
        )

    val charisSil: FontFamily
        @Composable
        get() = FontFamily(
            font("CharisSIL", "charissil_regular", FontWeight.Normal, FontStyle.Normal),
            font("CharisSIL", "charissil_bold", FontWeight.Bold, FontStyle.Normal),
            font("CharisSIL", "charissil_italic", FontWeight.Normal, FontStyle.Italic),
        )

    val comfortaa: FontFamily
        @Composable
        get() = FontFamily(
            font("Comfortaa", "comfortaa_regular", FontWeight.Normal, FontStyle.Normal),
            font("Comfortaa", "comfortaa_bold", FontWeight.Bold, FontStyle.Normal),
            font("Comfortaa", "comfortaa_medium", FontWeight.Medium, FontStyle.Normal),
            font("Comfortaa", "comfortaa_light", FontWeight.Light, FontStyle.Normal),
        )
}