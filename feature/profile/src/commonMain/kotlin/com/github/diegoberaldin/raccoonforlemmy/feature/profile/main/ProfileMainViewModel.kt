package com.github.diegoberaldin.raccoonforlemmy.feature.profile.main

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase.LogoutUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileMainViewModel(
    private val identityRepository: IdentityRepository,
    private val logout: LogoutUseCase,
) : ProfileMainMviModel,
    DefaultMviModel<ProfileMainMviModel.Intent, ProfileMainMviModel.UiState, ProfileMainMviModel.Effect>(
        initialState = ProfileMainMviModel.UiState(),
    ) {

    init {
        screenModelScope.launch {
            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(logged = logged) }
            }.launchIn(this)
        }
    }

    override fun reduce(intent: ProfileMainMviModel.Intent) {
        when (intent) {
            ProfileMainMviModel.Intent.Logout -> handleLogout()
        }
    }

    private fun handleLogout() {
        screenModelScope.launch {
            logout()
        }
    }
}
