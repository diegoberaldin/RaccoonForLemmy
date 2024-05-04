package com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileNotLoggedViewModel(
    private val identityRepository: IdentityRepository,
) : ProfileNotLoggedMviModel,
    DefaultMviModel<ProfileNotLoggedMviModel.Intent, ProfileNotLoggedMviModel.State, ProfileNotLoggedMviModel.Effect>(
        initialState = ProfileNotLoggedMviModel.State()
    ) {

    init {
        screenModelScope.launch {
            identityRepository.isLogged.onEach { logged ->
                val auth = identityRepository.authToken.value
                updateState { it.copy(authError = !auth.isNullOrEmpty() && logged == false) }
            }.launchIn(this)
        }
    }

    override fun reduce(intent: ProfileNotLoggedMviModel.Intent) {
        when (intent) {
            ProfileNotLoggedMviModel.Intent.Retry -> screenModelScope.launch {
                identityRepository.refreshLoggedState()
            }
        }
    }
}