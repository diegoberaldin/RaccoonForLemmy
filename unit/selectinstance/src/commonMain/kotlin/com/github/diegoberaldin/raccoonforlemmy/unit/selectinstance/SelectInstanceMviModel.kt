package com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface SelectInstanceMviModel :
    MviModel<SelectInstanceMviModel.Intent, SelectInstanceMviModel.State, SelectInstanceMviModel.Effect>,
    ScreenModel {
    sealed interface Intent
    data class State(
        val instances: List<String> = emptyList(),
    )

    sealed interface Effect
}