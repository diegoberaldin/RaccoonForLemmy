package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface FontScale {
    data object Largest : FontScale
    data object Larger : FontScale
    data object Large : FontScale
    data object Normal : FontScale
    data object Small : FontScale
    data object Smaller : FontScale
    data object Smallest : FontScale
}

val FontScale.scaleFactor: Float
    get() = when (this) {
        FontScale.Largest -> ReferenceValues.largest
        FontScale.Larger -> ReferenceValues.larger
        FontScale.Large -> ReferenceValues.large
        FontScale.Normal -> ReferenceValues.normal
        FontScale.Small -> ReferenceValues.small
        FontScale.Smaller -> ReferenceValues.smaller
        FontScale.Smallest -> ReferenceValues.smallest
    }

@Composable
fun FontScale.toReadableName(): String = when (this) {
    FontScale.Largest -> stringResource(MR.strings.settings_content_font_largest)
    FontScale.Larger -> stringResource(MR.strings.settings_content_font_larger)
    FontScale.Large -> stringResource(MR.strings.settings_content_font_large)
    FontScale.Normal -> stringResource(MR.strings.settings_content_font_normal)
    FontScale.Small -> stringResource(MR.strings.settings_content_font_small)
    FontScale.Smaller -> stringResource(MR.strings.settings_content_font_smaller)
    FontScale.Smallest -> stringResource(MR.strings.settings_content_font_smallest)
}

fun Float.toFontScale(): FontScale = when (this) {
    ReferenceValues.largest -> FontScale.Largest
    ReferenceValues.larger -> FontScale.Larger
    ReferenceValues.large -> FontScale.Large
    ReferenceValues.small -> FontScale.Small
    ReferenceValues.smaller -> FontScale.Smaller
    ReferenceValues.smallest -> FontScale.Smallest
    else -> FontScale.Normal
}

private object ReferenceValues {
    const val largest = 1.953125f
    const val larger = 1.5625f
    const val large = 1.25f
    const val normal = 1f
    const val small = 0.8f
    const val smaller = 0.64f
    const val smallest = 0.512f
}
