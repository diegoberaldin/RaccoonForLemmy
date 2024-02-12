package com.github.diegoberaldin.raccoonforlemmy.core.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import raccoon_for_lemmy.core.resources.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
object CoreResources {

    val matrix: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_matrix)

    val github: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_github)

    val lemmy: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_lemmy)


    val notoSans: FontFamily
        @Composable
        get() = FontFamily(
            Font(Res.font.notosans_regular, FontWeight.Normal, FontStyle.Normal),
            Font(Res.font.notosans_bold, FontWeight.Bold, FontStyle.Normal),
            Font(Res.font.notosans_medium, FontWeight.Medium, FontStyle.Normal),
            Font(Res.font.notosans_italic, FontWeight.Normal, FontStyle.Italic),
        )

    val poppins: FontFamily
        @Composable
        get() = FontFamily(
            Font(Res.font.poppins_regular, FontWeight.Normal, FontStyle.Normal),
            Font(Res.font.poppins_bold, FontWeight.Bold, FontStyle.Normal),
            Font(Res.font.poppins_medium, FontWeight.Medium, FontStyle.Normal),
            Font(Res.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
        )

    val charisSil: FontFamily
        @Composable
        get() = FontFamily(
            Font(Res.font.charissil_regular, FontWeight.Normal, FontStyle.Normal),
            Font(Res.font.charissil_bold, FontWeight.Bold, FontStyle.Normal),
            Font(Res.font.charissil_italic, FontWeight.Normal, FontStyle.Italic),
        )

    val comfortaa: FontFamily
        @Composable
        get() = FontFamily(
            Font(Res.font.comfortaa_regular, FontWeight.Normal, FontStyle.Normal),
            Font(Res.font.comfortaa_bold, FontWeight.Bold, FontStyle.Normal),
            Font(Res.font.comfortaa_medium, FontWeight.Medium, FontStyle.Normal),
            Font(Res.font.comfortaa_light, FontWeight.Light, FontStyle.Normal),
        )
}