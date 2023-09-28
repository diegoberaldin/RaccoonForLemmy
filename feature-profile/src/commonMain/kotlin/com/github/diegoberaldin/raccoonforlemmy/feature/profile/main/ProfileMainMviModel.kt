package com.github.diegoberaldin.raccoonforlemmy.feature.profile.main

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface ProfileMainMviModel :
    MviModel<ProfileMainMviModel.Intent, ProfileMainMviModel.UiState, ProfileMainMviModel.Effect> {

    sealed interface Intent {
        data object Logout : Intent
    }

    data class UiState(
        val logged: Boolean? = null,
    )

    sealed interface Effect
}
