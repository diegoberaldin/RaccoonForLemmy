package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel

interface ManageAccountsMviModel :
    MviModel<ManageAccountsMviModel.Intent, ManageAccountsMviModel.UiState, ManageAccountsMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class SwitchAccount(val index: Int) : Intent
        data class DeleteAccount(val index: Int) : Intent
    }

    data class UiState(
        val accounts: List<AccountModel> = emptyList(),
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect {
        data object Close : Effect
    }
}