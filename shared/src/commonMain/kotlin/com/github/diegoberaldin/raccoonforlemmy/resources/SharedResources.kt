package com.github.diegoberaldin.raccoonforlemmy.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.github.diegoberaldin.raccoonforlemmy.core.resources.CoreResources
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import raccoon_for_lemmy.shared.generated.resources.Res
import raccoon_for_lemmy.shared.generated.resources.charissil_bold
import raccoon_for_lemmy.shared.generated.resources.charissil_italic
import raccoon_for_lemmy.shared.generated.resources.charissil_regular
import raccoon_for_lemmy.shared.generated.resources.ic_alt_1
import raccoon_for_lemmy.shared.generated.resources.ic_alt_2
import raccoon_for_lemmy.shared.generated.resources.ic_default
import raccoon_for_lemmy.shared.generated.resources.ic_github
import raccoon_for_lemmy.shared.generated.resources.ic_lemmy
import raccoon_for_lemmy.shared.generated.resources.notosans_bold
import raccoon_for_lemmy.shared.generated.resources.notosans_italic
import raccoon_for_lemmy.shared.generated.resources.notosans_medium
import raccoon_for_lemmy.shared.generated.resources.notosans_regular
import raccoon_for_lemmy.shared.generated.resources.poppins_bold
import raccoon_for_lemmy.shared.generated.resources.poppins_italic
import raccoon_for_lemmy.shared.generated.resources.poppins_medium
import raccoon_for_lemmy.shared.generated.resources.poppins_regular

internal class SharedResources : CoreResources {
    override val github: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_github)

    override val lemmy: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_lemmy)

    override val appIconDefault: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_default)

    override val appIconAlt1: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_alt_1)

    override val appIconAlt2: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_alt_2)

    override val notoSans: FontFamily
        @Composable
        get() =
            FontFamily(
                Font(Res.font.notosans_regular, FontWeight.Normal, FontStyle.Normal),
                Font(Res.font.notosans_bold, FontWeight.Bold, FontStyle.Normal),
                Font(Res.font.notosans_medium, FontWeight.Medium, FontStyle.Normal),
                Font(Res.font.notosans_italic, FontWeight.Normal, FontStyle.Italic),
            )

    override val poppins: FontFamily
        @Composable
        get() =
            FontFamily(
                Font(Res.font.poppins_regular, FontWeight.Normal, FontStyle.Normal),
                Font(Res.font.poppins_bold, FontWeight.Bold, FontStyle.Normal),
                Font(Res.font.poppins_medium, FontWeight.Medium, FontStyle.Normal),
                Font(Res.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
            )

    override val charisSil: FontFamily
        @Composable
        get() =
            FontFamily(
                Font(Res.font.charissil_regular, FontWeight.Normal, FontStyle.Normal),
                Font(Res.font.charissil_bold, FontWeight.Bold, FontStyle.Normal),
                Font(Res.font.charissil_italic, FontWeight.Normal, FontStyle.Italic),
            )
}
