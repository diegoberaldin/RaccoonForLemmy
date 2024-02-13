package com.github.diegoberaldin.raccoonforlemmy.core.l10n

import cafe.adriel.lyricist.Lyricist

interface L10nManager {

    val lyricist: Lyricist<XmlStrings>

    fun changeLanguage(lang: String)
}
