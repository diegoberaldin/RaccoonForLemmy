package com.github.diegoberaldin.raccoonforlemmy.feature.profile.main

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.LogoutUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileMainViewModel(
    private val mvi: DefaultMviModel<ProfileMainMviModel.Intent, ProfileMainMviModel.UiState, ProfileMainMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val logout: LogoutUseCase,
    private val settingsRepository: SettingsRepository,
) : ScreenModel,
    MviModel<ProfileMainMviModel.Intent, ProfileMainMviModel.UiState, ProfileMainMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch {
            identityRepository.isLogged.stateIn(this).onEach { logged ->
                mvi.updateState { it.copy(logged = logged) }
            }.launchIn(this)
        }
    }

    override fun reduce(intent: ProfileMainMviModel.Intent) {
        when (intent) {
            ProfileMainMviModel.Intent.Logout -> handleLogout()
        }
    }

    private fun handleLogout() {
        mvi.scope?.launch {
            logout()
        }
    }
}
