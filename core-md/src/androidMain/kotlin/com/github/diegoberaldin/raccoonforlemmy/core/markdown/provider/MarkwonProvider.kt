package com.github.diegoberaldin.raccoonforlemmy.core.markdown.provider

import io.noties.markwon.Markwon

interface MarkwonProvider {
    val markwon: Markwon
}