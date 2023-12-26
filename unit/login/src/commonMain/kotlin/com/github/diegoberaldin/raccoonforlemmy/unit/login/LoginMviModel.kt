package com.github.diegoberaldin.raccoonforlemmy.unit.login

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import dev.icerock.moko.resources.desc.StringDesc

interface LoginMviModel :
    MviModel<LoginMviModel.Intent, LoginMviModel.UiState, LoginMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class SetInstanceName(val value: String) : Intent
        data class SetUsername(val value: String) : Intent
        data class SetPassword(val value: String) : Intent
        data class SetTotp2faToken(val value: String) : Intent

        data object Confirm : Intent
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
        data object LoginSuccess : Effect
    }
}
