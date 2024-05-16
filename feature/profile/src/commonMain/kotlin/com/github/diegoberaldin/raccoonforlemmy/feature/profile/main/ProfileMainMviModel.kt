package com.github.diegoberaldin.raccoonforlemmy.feature.profile.main

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

interface ProfileMainMviModel :
    MviModel<ProfileMainMviModel.Intent, ProfileMainMviModel.UiState, ProfileMainMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object Logout : Intent
    }

    data class UiState(
        val logged: Boolean? = null,
        val user: UserModel? = null,
    )

    sealed interface Effect
}
