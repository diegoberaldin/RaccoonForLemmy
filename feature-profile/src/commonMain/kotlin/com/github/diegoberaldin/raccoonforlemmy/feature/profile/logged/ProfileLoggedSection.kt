package com.github.diegoberaldin.raccoonforlemmy.feature.profile.logged

sealed interface ProfileLoggedSection {
    data object Posts : ProfileLoggedSection
    data object Comments : ProfileLoggedSection
}
