package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

sealed interface ProfileLoggedSection {
    data object Posts : ProfileLoggedSection

    data object Comments : ProfileLoggedSection
}
