package com.github.diegoberaldin.raccoonforlemmy.feature.profile.login

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import dev.icerock.moko.resources.desc.StringDesc

interface LoginBottomSheetMviModel :
    MviModel<LoginBottomSheetMviModel.Intent, LoginBottomSheetMviModel.UiState, LoginBottomSheetMviModel.Effect> {
    sealed interface Intent {
        data class SetInstanceName(val value: String) : Intent
        data class SetUsername(val value: String) : Intent
        data class SetPassword(val value: String) : Intent
        data class SetTotp2faToken(val value: String) : Intent

        object Confirm : Intent
    }

    data class UiState(
        val username: String = "",
        val usernameError: StringDesc? = null,
        val password: String = "",
        val passwordError: StringDesc? = null,
        val instanceName: String = "",
        val instanceNameError: StringDesc? = null,
        val totp2faToken: String = "",
        val loading: Boolean = false,
    )

    sealed interface Effect {
        data class LoginError(val message: String?) : Effect
        object LoginSuccess : Effect
    }
}
