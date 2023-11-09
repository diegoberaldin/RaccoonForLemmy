package com.github.diegoberaldin.raccoonforlemmy.feature.profile.login

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.LoginUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class LoginBottomSheetViewModel(
    private val mvi: DefaultMviModel<LoginBottomSheetMviModel.Intent, LoginBottomSheetMviModel.UiState, LoginBottomSheetMviModel.Effect>,
    private val login: LoginUseCase,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val accountRepository: AccountRepository,
    private val siteRepository: SiteRepository,
    private val communityRepository: CommunityRepository,
) : LoginBottomSheetMviModel,
    MviModel<LoginBottomSheetMviModel.Intent, LoginBottomSheetMviModel.UiState, LoginBottomSheetMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        val instance = apiConfigurationRepository.instance.value
        mvi.updateState {
            it.copy(instanceName = instance)
        }
    }

    override fun reduce(intent: LoginBottomSheetMviModel.Intent) {
        when (intent) {
            LoginBottomSheetMviModel.Intent.Confirm -> submit()
            is LoginBottomSheetMviModel.Intent.SetInstanceName -> setInstanceName(intent.value)
            is LoginBottomSheetMviModel.Intent.SetPassword -> setPassword(intent.value)
            is LoginBottomSheetMviModel.Intent.SetTotp2faToken -> setTotp2faToken(intent.value)
            is LoginBottomSheetMviModel.Intent.SetUsername -> setUsername(intent.value)
        }
    }

    private fun setInstanceName(value: String) {
        mvi.updateState { it.copy(instanceName = value) }
    }

    private fun setUsername(value: String) {
        mvi.updateState { it.copy(username = value) }
    }

    private fun setPassword(value: String) {
        mvi.updateState { it.copy(password = value) }
    }

    private fun setTotp2faToken(value: String) {
        mvi.updateState { it.copy(totp2faToken = value) }
    }

    private fun submit() {
        val currentState = uiState.value
        if (currentState.loading) {
            return
        }

        val instance = currentState.instanceName
        val username = currentState.username
        val password = currentState.password
        val totp2faToken = currentState.totp2faToken
        mvi.updateState {
            it.copy(
                instanceNameError = null,
                usernameError = null,
                passwordError = null,
            )
        }

        val valid = when {
            instance.isEmpty() -> {
                mvi.updateState {
                    it.copy(
                        instanceNameError = MR.strings.message_missing_field.desc(),
                    )
                }
                false
            }

            username.isEmpty() -> {
                mvi.updateState {
                    it.copy(
                        usernameError = MR.strings.message_missing_field.desc(),
                    )
                }
                false
            }

            password.isEmpty() -> {
                mvi.updateState {
                    it.copy(
                        passwordError = MR.strings.message_missing_field.desc(),
                    )
                }
                false
            }

            else -> true
        }
        if (!valid) {
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }

            val res = communityRepository.getAll(
                instance = instance,
                page = 1,
                limit = 1,
                resultType = SearchResultType.Communities,
            ) ?: emptyList()
            if (res.isEmpty()) {
                mvi.updateState {
                    it.copy(
                        instanceNameError = MR.strings.message_invalid_field.desc(),
                        loading = false,
                    )
                }
                return@launch
            }

            val result = login(
                instance = instance,
                username = username,
                password = password,
                totp2faToken = totp2faToken,
            )
            mvi.updateState { it.copy(loading = false) }

            if (result.isFailure) {
                result.exceptionOrNull()?.also {
                    val message = it.message
                    mvi.emitEffect(LoginBottomSheetMviModel.Effect.LoginError(message))
                }
            } else {
                val accountId = accountRepository.getActive()?.id
                if (accountId != null) {
                    val auth = identityRepository.authToken.value.orEmpty()
                    val avatar = siteRepository.getCurrentUser(auth = auth)?.avatar
                    accountRepository.update(
                        id = accountId,
                        avatar = avatar,
                        jwt = auth
                    )
                }
                mvi.emitEffect(LoginBottomSheetMviModel.Effect.LoginSuccess)
            }
        }
    }
}
