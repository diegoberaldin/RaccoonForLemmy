package com.github.diegoberaldin.raccoonforlemmy.unit.ban

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ValidationError

@Stable
interface BanUserMviModel :
    MviModel<BanUserMviModel.Intent, BanUserMviModel.UiState, BanUserMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class SetText(val value: String) : Intent

        data class ChangePermanent(val value: Boolean) : Intent

        data class ChangeRemoveData(val value: Boolean) : Intent

        data object DecrementDays : Intent

        data object IncrementDays : Intent

        data object Submit : Intent
    }

    data class UiState(
        val permanent: Boolean = true,
        val days: Int = 1,
        val targetBanValue: Boolean = false,
        val removeData: Boolean = false,
        val text: String = "",
        val textError: ValidationError? = null,
        val loading: Boolean = false,
    )

    sealed interface Effect {
        data object Success : Effect

        data class Failure(val message: String?) : Effect
    }
}
