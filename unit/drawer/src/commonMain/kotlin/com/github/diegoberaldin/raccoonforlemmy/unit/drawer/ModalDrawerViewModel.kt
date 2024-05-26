package com.github.diegoberaldin.raccoonforlemmy.unit.drawer

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.FavoriteCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

class ModalDrawerViewModel(
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val accountRepository: AccountRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val siteRepository: SiteRepository,
    private val apiConfigurationRepository: ApiConfigurationRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteCommunityRepository: FavoriteCommunityRepository,
    private val notificationCenter: NotificationCenter,
) : ModalDrawerMviModel,
    DefaultMviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect>(
        initialState = ModalDrawerMviModel.UiState(),
    ) {
    private val searchEventChannel = Channel<Unit>()

    init {
        screenModelScope.launch {
            apiConfigurationRepository.instance.onEach { instance ->
                updateState {
                    it.copy(instance = instance)
                }
            }.launchIn(this)

            identityRepository.isLogged.onEach { _ ->
                refreshUser()
                refresh()
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                delay(250)
                refreshUser()
                refresh()
            }.launchIn(this)

            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                    )
                }
            }.launchIn(this)

            @OptIn(FlowPreview::class)
            searchEventChannel.receiveAsFlow().debounce(1000).onEach {
                refresh()
            }.launchIn(this)

            observeChangesInFavoriteCommunities()

            delay(250)
            refreshUser()
            refresh()
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
            val newCommunities =
                uiState.value.communities.map { community ->
                    community.copy(favorite = community.id in favoriteCommunityIds)
                }
                    .sortedBy { it.name }
                    .sortedByDescending { it.favorite }
            updateState { it.copy(communities = newCommunities) }
        }.launchIn(this)
    }

    override fun reduce(intent: ModalDrawerMviModel.Intent) {
        when (intent) {
            ModalDrawerMviModel.Intent.Refresh ->
                screenModelScope.launch {
                    refresh()
                }

            is ModalDrawerMviModel.Intent.SetSearch -> {
                screenModelScope.launch {
                    updateState { it.copy(searchText = intent.value) }
                    searchEventChannel.send(Unit)
                }
            }
        }
    }

    private suspend fun refreshUser() {
        val auth = identityRepository.authToken.value.orEmpty()
        if (auth.isEmpty()) {
            updateState { it.copy(user = null) }
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
                    updateState { it.copy(user = user) }
                }
            }
        }
    }

    private suspend fun refresh() {
        if (uiState.value.refreshing) {
            return
        }
        updateState { it.copy(refreshing = true) }

        val auth = identityRepository.authToken.value
        val accountId = accountRepository.getActive()?.id
        val favoriteCommunityIds =
            favoriteCommunityRepository.getAll(accountId).map { it.communityId }
        val searchText = uiState.value.searchText
        val communities =
            communityRepository.getSubscribed(auth)
                .let {
                    if (searchText.isEmpty()) {
                        it
                    } else {
                        it.filter { e ->
                            listOf(e.name, e.title).any { s -> s.contains(other = searchText, ignoreCase = true) }
                        }
                    }
                }.map { community ->
                    community.copy(favorite = community.id in favoriteCommunityIds)
                }
                .sortedBy { it.name }
                .let {
                    val favorites = it.filter { e -> e.favorite }
                    val res = it - favorites.toSet()
                    favorites + res
                }
        val multiCommunitites =
            accountId?.let {
                multiCommunityRepository.getAll(it)
                    .let { communities ->
                        if (searchText.isEmpty()) {
                            communities
                        } else {
                            communities.filter { c -> c.name.contains(other = searchText, ignoreCase = true) }
                        }
                    }
                    .sortedBy { e -> e.name }
            }.orEmpty()
        updateState {
            it.copy(
                isFiltering = searchText.isNotEmpty(),
                refreshing = false,
                communities = communities,
                multiCommunities = multiCommunitites,
            )
        }
    }
}
