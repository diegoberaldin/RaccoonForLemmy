package com.github.diegoberaldin.raccoonforlemmy.unit.manageban

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageBanViewModel(
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
) : ManageBanMviModel,
    DefaultMviModel<ManageBanMviModel.Intent, ManageBanMviModel.UiState, ManageBanMviModel.Effect>(
        initialState = ManageBanMviModel.UiState(),
    ) {

    init {
        screenModelScope.launch {
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                    )
                }
            }.launchIn(this)

            withContext(Dispatchers.IO) {
                if (uiState.value.initial) {
                    refresh()
                }
            }
        }
    }

    override fun reduce(intent: ManageBanMviModel.Intent) {
        when (intent) {
            is ManageBanMviModel.Intent.ChangeSection -> {
                updateState { it.copy(section = intent.section) }
            }

            ManageBanMviModel.Intent.Refresh -> {
                screenModelScope.launch {
                    refresh()
                }
            }

            is ManageBanMviModel.Intent.UnblockCommunity -> unbanCommunity(intent.id)
            is ManageBanMviModel.Intent.UnblockInstance -> unbanInstance(intent.id)
            is ManageBanMviModel.Intent.UnblockUser -> unbanUser(intent.id)
        }
    }

    private suspend fun refresh() {
        val auth = identityRepository.authToken.value.orEmpty()
        val bans = siteRepository.getBans(auth)
        updateState {
            it.copy(
                bannedUsers = bans?.users.orEmpty(),
                bannedCommunities = bans?.communities.orEmpty(),
                bannedInstances = bans?.instances.orEmpty(),
                initial = false
            )
        }
    }

    private fun unbanUser(id: Int) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            runCatching {
                userRepository.block(
                    id = id,
                    blocked = false,
                    auth = auth
                )
                updateState {
                    it.copy(bannedUsers = it.bannedUsers.filter { e -> e.id != id })
                }
            }
        }
    }

    private fun unbanCommunity(id: Int) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            runCatching {
                communityRepository.block(
                    id = id,
                    blocked = false,
                    auth = auth
                )
                updateState {
                    it.copy(bannedCommunities = it.bannedCommunities.filter { e -> e.id != id })
                }
            }
        }
    }

    private fun unbanInstance(id: Int) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            runCatching {
                siteRepository.block(
                    id = id,
                    blocked = false,
                    auth = auth
                )
                updateState {
                    it.copy(bannedInstances = it.bannedInstances.filter { e -> e.id != id })
                }
            }
        }
    }
}