package com.github.diegoberaldin.raccoonforlemmy.unit.moderatewithreason

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ValidationError

sealed interface ModerateWithReasonAction {
    data object HideCommunity : ModerateWithReasonAction
    data object PurgeComment : ModerateWithReasonAction
    data object PurgeCommunity : ModerateWithReasonAction
    data object PurgePost : ModerateWithReasonAction
    data object PurgeUser : ModerateWithReasonAction
    data object RemoveComment : ModerateWithReasonAction
    data object RemovePost : ModerateWithReasonAction
    data object ReportComment : ModerateWithReasonAction
    data object ReportPost : ModerateWithReasonAction
}

fun ModerateWithReasonAction.toInt(): Int = when (this) {
    ModerateWithReasonAction.HideCommunity -> 0
    ModerateWithReasonAction.PurgeComment -> 1
    ModerateWithReasonAction.PurgeCommunity -> 2
    ModerateWithReasonAction.PurgePost -> 3
    ModerateWithReasonAction.PurgeUser -> 4
    ModerateWithReasonAction.RemoveComment -> 5
    ModerateWithReasonAction.RemovePost -> 6
    ModerateWithReasonAction.ReportComment -> 7
    ModerateWithReasonAction.ReportPost -> 8
}

internal fun Int.toModerateWithReasonAction(): ModerateWithReasonAction = when (this) {
    8 -> ModerateWithReasonAction.ReportPost
    7 -> ModerateWithReasonAction.ReportComment
    6 -> ModerateWithReasonAction.RemovePost
    5 -> ModerateWithReasonAction.RemoveComment
    4 -> ModerateWithReasonAction.PurgeUser
    3 -> ModerateWithReasonAction.PurgePost
    2 -> ModerateWithReasonAction.PurgeCommunity
    1 -> ModerateWithReasonAction.PurgeComment
    else -> ModerateWithReasonAction.HideCommunity
}

@Stable
interface ModerateWithReasonMviModel :
    MviModel<ModerateWithReasonMviModel.Intent, ModerateWithReasonMviModel.UiState, ModerateWithReasonMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class SetText(val value: String) : Intent
        data object Submit : Intent
    }

    data class UiState(
        val action: ModerateWithReasonAction = ModerateWithReasonAction.ReportPost,
        val text: String = "",
        val textError: ValidationError? = null,
        val loading: Boolean = false,
    )

    sealed interface Effect {
        data object Success : Effect
        data class Failure(val message: String?) : Effect
    }
}
