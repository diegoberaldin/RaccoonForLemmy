package com.github.diegoberaldin.raccoonforlemmy

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface MainScreenMviModel :
    MviModel<MainScreenMviModel.Intent, MainScreenMviModel.UiState, MainScreenMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class SetBottomBarOffsetHeightPx(
            val value: Float,
        ) : Intent
    }

    data class UiState(
        val bottomBarOffsetHeightPx: Float = 0f,
        val customProfileUrl: String? = null,
    )

    sealed interface Effect {
        data class UnreadItemsDetected(
            val value: Int,
        ) : Effect
    }
}
