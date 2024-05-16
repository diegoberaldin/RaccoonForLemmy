package com.github.diegoberaldin.raccoonforlemmy.unit.licences

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.licences.models.LicenceItem

interface LicencesMviModel :
    ScreenModel,
    MviModel<LicencesMviModel.Intent, LicencesMviModel.State, LicencesMviModel.Effect> {
    sealed interface Intent

    data class State(
        val items: List<LicenceItem> = emptyList(),
    )

    sealed interface Effect
}
