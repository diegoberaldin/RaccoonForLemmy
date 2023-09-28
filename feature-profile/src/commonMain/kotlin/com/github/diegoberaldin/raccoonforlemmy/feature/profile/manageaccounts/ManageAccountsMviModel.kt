package com.github.diegoberaldin.raccoonforlemmy.feature.profile.manageaccounts

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel

interface ManageAccountsMviModel :
    MviModel<ManageAccountsMviModel.Intent, ManageAccountsMviModel.UiState, ManageAccountsMviModel.Effect> {
    sealed interface Intent {
        data class SwitchAccount(val index: Int) : Intent
    }

    data class UiState(
        val accounts: List<AccountModel> = emptyList(),
    )

    sealed interface Effect {
        data object Close : Effect
    }
}