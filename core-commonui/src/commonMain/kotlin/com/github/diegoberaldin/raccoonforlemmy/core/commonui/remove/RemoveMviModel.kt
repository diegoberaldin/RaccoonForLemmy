package com.github.diegoberaldin.raccoonforlemmy.core.commonui.remove

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import dev.icerock.moko.resources.desc.StringDesc

@Stable
interface RemoveMviModel :
    MviModel<RemoveMviModel.Intent, RemoveMviModel.UiState, RemoveMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class SetText(val value: String) : Intent
        data object Submit : Intent
    }

    data class UiState(
        val text: String = "",
        val textError: StringDesc? = null,
        val loading: Boolean = false,
    )

    sealed interface Effect {
        data object Success : Effect

        data class Failure(val message: String?) : Effect
    }
}
