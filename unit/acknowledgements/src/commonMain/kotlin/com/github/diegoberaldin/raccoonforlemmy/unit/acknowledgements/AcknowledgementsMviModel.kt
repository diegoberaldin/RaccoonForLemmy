package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.models.AcknowledgementModel

interface AcknowledgementsMviModel :
    ScreenModel,
    MviModel<AcknowledgementsMviModel.Intent, AcknowledgementsMviModel.State, AcknowledgementsMviModel.Effect> {
    sealed interface Intent {
        data object Refresh : Intent
    }

    data class State(
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val items: List<AcknowledgementModel> = emptyList(),
    )

    sealed interface Effect
}
