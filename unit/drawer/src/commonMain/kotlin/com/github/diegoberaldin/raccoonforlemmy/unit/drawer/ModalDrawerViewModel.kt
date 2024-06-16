package com.github.diegoberaldin.raccoonforlemmy.unit.drawer

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.FavoriteCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.FavoriteCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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
) : DefaultMviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect>(
        initialState = ModalDrawerMviModel.UiState(),
    ),
    ModalDrawerMviModel {
    private var currentPage = 1
    private val searchEventChannel = Channel<Unit>()

    init {
        screenModelScope.launch {
            apiConfigurationRepository.instance
                .onEach { instance ->
                    updateState {
                        it.copy(instance = instance)
                    }
                }.launchIn(this)

            identityRepository.isLogged
                .onEach { _ ->
                    refreshUser()
                    refresh()
                }.launchIn(this)

            notificationCenter
                .subscribe(NotificationCenterEvent.Logout::class)
                .onEach {
                    delay(250)
                    refreshUser()
                    refresh()
                }.launchIn(this)

            notificationCenter
                .subscribe(NotificationCenterEvent.FavoritesUpdated::class)
                .onEach {
                    refresh()
                }.launchIn(this)

            settingsRepository.currentSettings
                .onEach { settings ->
                    updateState {
                        it.copy(
                            autoLoadImages = settings.autoLoadImages,
                            preferNicknames = settings.preferUserNicknames,
                        )
                    }
                }.launchIn(this)

            @OptIn(FlowPreview::class)
            searchEventChannel
                .receiveAsFlow()
                .debounce(1000)
                .onEach {
                    refresh()
                }.launchIn(this)

            delay(250)
            refreshUser()
            refresh()
        }
    }

    override fun reduce(intent: ModalDrawerMviModel.Intent) {
        when (intent) {
            ModalDrawerMviModel.Intent.Refresh ->
                screenModelScope.launch {
                    refresh()
                }

            is ModalDrawerMviModel.Intent.SetSearch ->
                screenModelScope.launch {
                    updateState { it.copy(searchText = intent.value) }
                    searchEventChannel.send(Unit)
                }

            ModalDrawerMviModel.Intent.LoadNextPage ->
                screenModelScope.launch {
                    loadNextPage()
                }

            is ModalDrawerMviModel.Intent.ToggleFavorite -> toggleFavorite(intent.id)
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
        currentPage = 1
        updateState {
            it.copy(
                refreshing = true,
                canFetchMore = true,
                loading = false,
            )
        }

        val accountId = accountRepository.getActive()?.id
        val searchText = uiState.value.searchText
        val multiCommunities =
            accountId
                ?.let {
                    multiCommunityRepository
                        .getAll(it)
                        .let { communities ->
                            if (searchText.isEmpty()) {
                                communities
                            } else {
                                communities.filter { c ->
                                    c.name.contains(
                                        other = searchText,
                                        ignoreCase = true,
                                    )
                                }
                            }
                        }.sortedBy { e -> e.name }
                }.orEmpty()

        val favorites =
            coroutineScope {
                val auth = identityRepository.authToken.value
                favoriteCommunityRepository.getAll(accountId).mapNotNull { favorite ->
                    val communityId = favorite.communityId
                    async {
                        communityRepository.get(
                            auth = auth,
                            id = communityId,
                        )
                    }.await()
                }
            }.let { communities ->
                if (searchText.isEmpty()) {
                    communities
                } else {
                    communities.filter { c ->
                        c.name.contains(
                            other = searchText,
                            ignoreCase = true,
                        )
                    }
                }
            }

        updateState {
            it.copy(
                multiCommunities = multiCommunities,
                favorites = favorites,
            )
        }

        loadNextPage()
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }
        val auth = identityRepository.authToken.value
        val searchText = uiState.value.searchText
        val itemsToAdd =
            communityRepository
                .getSubscribed(
                    auth = auth,
                    page = currentPage,
                    query = searchText,
                ).filter { c1 ->
                    // exclude items already included in favorites
                    currentState.favorites.none { c2 -> c2.id == c1.id }
                }.filter { c1 ->
                    // prevents accidental duplication
                    if (!currentState.refreshing) {
                        currentState.communities.none { c2 -> c2.id == c1.id }
                    } else {
                        true
                    }
                }
        if (itemsToAdd.isNotEmpty()) {
            currentPage++
        }
        updateState {
            it.copy(
                isFiltering = searchText.isNotEmpty(),
                refreshing = false,
                communities =
                    if (currentState.refreshing) {
                        itemsToAdd
                    } else {
                        currentState.communities + itemsToAdd
                    },
                canFetchMore = itemsToAdd.isNotEmpty(),
                loading = false,
            )
        }
    }

    private fun toggleFavorite(communityId: Long) {
        screenModelScope.launch {
            val currentState = uiState.value
            val accountId = accountRepository.getActive()?.id ?: 0L
            val isCurrentlyFavorite = currentState.favorites.any { it.id == communityId }
            if (isCurrentlyFavorite) {
                val community = currentState.favorites.first { it.id == communityId }
                favoriteCommunityRepository.getBy(accountId, communityId)?.also { toDelete ->
                    favoriteCommunityRepository.delete(accountId, toDelete)
                }
                val newFavorites = currentState.favorites.filter { it.id != communityId }
                val newCommunities = currentState.communities + community
                updateState {
                    it.copy(
                        favorites = newFavorites,
                        communities = newCommunities,
                    )
                }
            } else {
                val model = FavoriteCommunityModel(communityId = communityId)
                favoriteCommunityRepository.create(model, accountId)
                val community = currentState.communities.first { it.id == communityId }
                val newFavorites = currentState.favorites + community
                val newCommunities = currentState.communities.filter { it.id != communityId }
                updateState {
                    it.copy(
                        favorites = newFavorites,
                        communities = newCommunities,
                    )
                }
            }
        }
    }
}
