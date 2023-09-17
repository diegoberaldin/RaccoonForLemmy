package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged

sealed interface ProfileLoggedSection {
    object Posts : ProfileLoggedSection
    object Comments : ProfileLoggedSection
}
