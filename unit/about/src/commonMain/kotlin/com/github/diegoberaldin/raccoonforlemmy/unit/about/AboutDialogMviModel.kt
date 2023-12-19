package com.github.diegoberaldin.raccoonforlemmy.unit.about

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

interface AboutDialogMviModel :
    MviModel<AboutDialogMviModel.Intent, AboutDialogMviModel.UiState, AboutDialogMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object OpenOwnCommunity : Intent
    }

    data class UiState(
        val version: String = "",
    )

    sealed interface Effect {
        data class OpenCommunity(
            val community: CommunityModel,
            val instance: String = "",
        ) : Effect
    }

}