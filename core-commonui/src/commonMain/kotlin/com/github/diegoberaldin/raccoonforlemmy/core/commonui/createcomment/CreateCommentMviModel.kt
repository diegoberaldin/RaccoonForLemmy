package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface CreateCommentMviModel :
    MviModel<CreateCommentMviModel.Intent, CreateCommentMviModel.UiState, CreateCommentMviModel.Effect> {

    sealed interface Intent {
        data class SetText(val value: String) : Intent

        object Send : Intent
    }

    data class UiState(
        val text: String = "",
    )

    sealed interface Effect {
        object Success : Effect

        data class Failure(val message: String?) : Effect
    }
}
