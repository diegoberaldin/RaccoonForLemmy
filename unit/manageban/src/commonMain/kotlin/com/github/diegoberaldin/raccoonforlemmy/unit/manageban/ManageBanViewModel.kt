package com.github.diegoberaldin.raccoonforlemmy.unit.manageban

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
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
    private val mvi: DefaultMviModel<ManageBanMviModel.Intent, ManageBanMviModel.UiState, ManageBanMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
) : ManageBanMviModel,
    MviModel<ManageBanMviModel.Intent, ManageBanMviModel.UiState, ManageBanMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
            }.launchIn(this)

            withContext(Dispatchers.IO) {
                if (mvi.uiState.value.initial) {
                    refresh()
                }
            }
        }
    }

    override fun reduce(intent: ManageBanMviModel.Intent) {
        when (intent) {
            is ManageBanMviModel.Intent.ChangeSection -> {
                mvi.updateState { it.copy(section = intent.section) }
            }

            ManageBanMviModel.Intent.Refresh -> {
                mvi.scope?.launch(Dispatchers.IO) {
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
        mvi.updateState {
            it.copy(
                bannedUsers = bans?.users.orEmpty(),
                bannedCommunities = bans?.communities.orEmpty(),
                bannedInstances = bans?.instances.orEmpty(),
                initial = false
            )
        }
    }

    private fun unbanUser(id: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            runCatching {
                userRepository.block(
                    id = id,
                    blocked = false,
                    auth = auth
                )
                mvi.updateState {
                    it.copy(bannedUsers = it.bannedUsers.filter { e -> e.id != id })
                }
            }
        }
    }

    private fun unbanCommunity(id: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            runCatching {
                communityRepository.block(
                    id = id,
                    blocked = false,
                    auth = auth
                )
                mvi.updateState {
                    it.copy(bannedCommunities = it.bannedCommunities.filter { e -> e.id != id })
                }
            }
        }
    }

    private fun unbanInstance(id: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            runCatching {
                siteRepository.block(
                    id = id,
                    blocked = false,
                    auth = auth
                )
                mvi.updateState {
                    it.copy(bannedInstances = it.bannedInstances.filter { e -> e.id != id })
                }
            }
        }
    }
}