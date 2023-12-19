package com.github.diegoberaldin.raccoonforlemmy.feature.profile.main

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface ProfileMainMviModel :
    MviModel<ProfileMainMviModel.Intent, ProfileMainMviModel.UiState, ProfileMainMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object Logout : Intent
    }

    data class UiState(
        val logged: Boolean? = null,
    )

    sealed interface Effect
}
