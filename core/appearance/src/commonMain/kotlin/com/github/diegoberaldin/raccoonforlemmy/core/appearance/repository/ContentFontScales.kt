package com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository

data class ContentFontScales(
    val title: Float = 1f,
    val body: Float = 1f,
    val comment: Float = 1f,
    val ancillary: Float = 1f,
)

sealed interface ContentFontClass {
    data object Title : ContentFontClass
    data object Body : ContentFontClass
    data object Comment : ContentFontClass
    data object AncillaryText : ContentFontClass
}
