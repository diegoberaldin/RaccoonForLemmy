package com.github.diegoberaldin.raccoonforlemmy

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface MainMviModel :
    MviModel<MainMviModel.Intent, MainMviModel.UiState, MainMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class SetBottomBarOffsetHeightPx(
            val value: Float,
        ) : Intent

        data object ReadAllInbox : Intent
    }

    data class UiState(
        val bottomBarOffsetHeightPx: Float = 0f,
        val customProfileUrl: String? = null,
        val isLogged: Boolean = false,
    )

    sealed interface Effect {
        data class UnreadItemsDetected(
            val value: Int,
        ) : Effect

        data object ReadAllInboxSuccess : Effect
    }
}
