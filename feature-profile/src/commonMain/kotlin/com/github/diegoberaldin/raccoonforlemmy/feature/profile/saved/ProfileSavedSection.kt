package com.github.diegoberaldin.raccoonforlemmy.feature.profile.saved

sealed interface ProfileSavedSection {
    data object Posts : ProfileSavedSection

    data object Comments : ProfileSavedSection
}
