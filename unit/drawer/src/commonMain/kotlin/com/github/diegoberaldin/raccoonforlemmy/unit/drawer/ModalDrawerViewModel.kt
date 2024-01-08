package com.github.diegoberaldin.raccoonforlemmy.unit.drawer

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.FavoriteCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

class ModalDrawerViewModel(
    private val mvi: DefaultMviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val accountRepository: AccountRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val siteRepository: SiteRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteCommunityRepository: FavoriteCommunityRepository,
) : ModalDrawerMviModel,
    MviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect> by mvi {

    @OptIn(FlowPreview::class)
    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            apiConfigurationRepository.instance.onEach { instance ->
                mvi.updateState {
                    it.copy(instance = instance)
                }
            }.launchIn(this)
            identityRepository.isLogged.debounce(250).onEach { _ ->
                refreshUser()
                refresh()
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
            }.launchIn(this)

            observeChangesInFavoriteCommunities()

            withContext(Dispatchers.IO) {
                delay(250)
                refreshUser()
                refresh()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.observeChangesInFavoriteCommunities() {
        channelFlow {
            while (isActive) {
                val accountId = accountRepository.getActive()?.id
                trySend(accountId)
                delay(1000)
            }
        }.distinctUntilChanged().flatMapConcat { accountId ->
            channelFlow {
                while (isActive) {
                    val communityIds =
                        favoriteCommunityRepository.getAll(accountId).map { it.communityId }
                    trySend(communityIds)
                    delay(1000)
                }
            }.distinctUntilChanged()
        }.onEach { favoriteCommunityIds ->
            val newCommunities = uiState.value.communities.map { community ->
                community.copy(favorite = community.id in favoriteCommunityIds)
            }
                .sortedBy { it.name }
                .sortedByDescending { it.favorite }
            mvi.updateState { it.copy(communities = newCommunities) }
        }.launchIn(this)
    }

    override fun reduce(intent: ModalDrawerMviModel.Intent) {
        when (intent) {
            ModalDrawerMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }
        }
    }

    private suspend fun refreshUser() {
        val auth = identityRepository.authToken.value.orEmpty()
        if (auth.isEmpty()) {
            mvi.updateState { it.copy(user = null) }
        } else {
            var user = siteRepository.getCurrentUser(auth)
            runCatching {
                withTimeout(2000) {
                    while (user == null) {
                        // retry getting user if non-empty auth
                        delay(500)
                        user = siteRepository.getCurrentUser(auth)
                        yield()
                    }
                    mvi.updateState { it.copy(user = user) }
                }
            }
        }
    }

    private suspend fun refresh() {
        if (uiState.value.refreshing) {
            return
        }
        mvi.updateState { it.copy(refreshing = true) }

        val auth = identityRepository.authToken.value
        val accountId = accountRepository.getActive()?.id
        val favoriteCommunityIds =
            favoriteCommunityRepository.getAll(accountId).map { it.communityId }
        val communities = communityRepository.getSubscribed(auth)
            .map { community ->
                community.copy(favorite = community.id in favoriteCommunityIds)
            }
            .sortedBy { it.name }
            .let {
                val favorites = it.filter { e -> e.favorite }
                val res = it - favorites.toSet()
                favorites + res
            }
        val multiCommunitites = accountId?.let {
            multiCommunityRepository.getAll(it).sortedBy { e -> e.name }
        }.orEmpty()
        mvi.updateState {
            it.copy(
                refreshing = false,
                communities = communities,
                multiCommunities = multiCommunitites,
            )
        }
    }
}