package com.github.diegoberaldin.raccoonforlemmy.core.l10n

import cafe.adriel.lyricist.Lyricist

internal class DefaultL10nManager : L10nManager {
    override val lyricist: Lyricist<XmlStrings> =
        Lyricist(
            defaultLanguageTag = "en",
            translations = xmlStrings,
        )

    override fun changeLanguage(lang: String) {
        lyricist.languageTag = lang
    }
}
