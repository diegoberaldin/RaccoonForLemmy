package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

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
    get() =
        when (this) {
            FontScale.Largest -> ReferenceValues.largest
            FontScale.Larger -> ReferenceValues.larger
            FontScale.Large -> ReferenceValues.large
            FontScale.Normal -> ReferenceValues.normal
            FontScale.Small -> ReferenceValues.small
            FontScale.Smaller -> ReferenceValues.smaller
            FontScale.Smallest -> ReferenceValues.smallest
        }

@Composable
fun FontScale.toReadableName(): String =
    when (this) {
        FontScale.Largest -> LocalXmlStrings.current.settingsContentFontLargest
        FontScale.Larger -> LocalXmlStrings.current.settingsContentFontLarger
        FontScale.Large -> LocalXmlStrings.current.settingsContentFontLarge
        FontScale.Normal -> LocalXmlStrings.current.settingsContentFontNormal
        FontScale.Small -> LocalXmlStrings.current.settingsContentFontSmall
        FontScale.Smaller -> LocalXmlStrings.current.settingsContentFontSmaller
        FontScale.Smallest -> LocalXmlStrings.current.settingsContentFontSmallest
    }

fun Float.toFontScale(): FontScale =
    when (this) {
        ReferenceValues.largest -> FontScale.Largest
        ReferenceValues.larger -> FontScale.Larger
        ReferenceValues.large -> FontScale.Large
        ReferenceValues.small -> FontScale.Small
        ReferenceValues.smaller -> FontScale.Smaller
        ReferenceValues.smallest -> FontScale.Smallest
        else -> FontScale.Normal
    }

// log(2 + 0.1 * n) / log(2)
private object ReferenceValues {
    const val largest = 1.2f
    const val larger = 1.14f
    const val large = 1.07f
    const val normal = 1f
    const val small = 0.93f
    const val smaller = 0.85f
    const val smallest = 0.77f
}
