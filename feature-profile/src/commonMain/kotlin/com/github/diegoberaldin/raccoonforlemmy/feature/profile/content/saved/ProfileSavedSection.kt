package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.saved

sealed interface ProfileSavedSection {
    object Posts : ProfileSavedSection

    object Comments : ProfileSavedSection
}
