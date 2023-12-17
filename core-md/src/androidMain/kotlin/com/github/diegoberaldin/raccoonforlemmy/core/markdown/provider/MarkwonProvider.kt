package com.github.diegoberaldin.raccoonforlemmy.core.markdown.provider

import io.noties.markwon.Markwon
import kotlinx.coroutines.flow.StateFlow

interface MarkwonProvider {
    val markwon: Markwon
    val blockClickPropagation: StateFlow<Boolean>
    var onOpenUrl: ((String) -> Unit)?
    var onOpenImage: ((String) -> Unit)?
}