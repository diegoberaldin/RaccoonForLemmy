package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

sealed interface CreatePostSection {
    data object Edit : CreatePostSection
    data object Preview : CreatePostSection
}
