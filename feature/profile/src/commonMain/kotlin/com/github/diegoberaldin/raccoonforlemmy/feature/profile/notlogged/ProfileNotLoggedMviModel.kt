package com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface ProfileNotLoggedMviModel :
    ScreenModel,
    MviModel<ProfileNotLoggedMviModel.Intent, ProfileNotLoggedMviModel.State, ProfileNotLoggedMviModel.Effect> {
    sealed interface Intent {
        data object Retry : Intent
    }

    data class State(val authError: Boolean = false)

    sealed interface Effect
}
