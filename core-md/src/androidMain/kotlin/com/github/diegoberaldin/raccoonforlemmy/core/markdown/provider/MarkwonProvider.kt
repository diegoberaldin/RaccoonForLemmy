package com.github.diegoberaldin.raccoonforlemmy.core.markdown.provider

import io.noties.markwon.Markwon
import kotlinx.coroutines.flow.StateFlow

interface MarkwonProvider {
    val markwon: Markwon
    val isHandlingLink: StateFlow<Boolean>
}