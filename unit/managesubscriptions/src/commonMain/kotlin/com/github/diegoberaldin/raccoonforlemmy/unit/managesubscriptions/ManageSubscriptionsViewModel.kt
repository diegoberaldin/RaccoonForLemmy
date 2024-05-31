package com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.FavoriteCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.FavoriteCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ManageSubscriptionsViewModel(
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val accountRepository: AccountRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val settingsRepository: SettingsRepository,
    private val favoriteCommunityRepository: FavoriteCommunityRepository,
    private val hapticFeedback: HapticFeedback,
    private val notificationCenter: NotificationCenter,
) : ManageSubscriptionsMviModel,
    DefaultMviModel<ManageSubscriptionsMviModel.Intent, ManageSubscriptionsMviModel.UiState, ManageSubscriptionsMviModel.Effect>(
        initialState = ManageSubscriptionsMviModel.UiState(),
    ) {
    private var currentPage = 1
    private val searchEventChannel = Channel<Unit>()

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
            notificationCenter.subscribe(NotificationCenterEvent.MultiCommunityCreated::class)
                .onEach { evt ->
                    handleMultiCommunityCreated(evt.model)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CommunitySubscriptionChanged::class)
                .onEach { evt ->
                    handleCommunityUpdate(evt.value)
                }.launchIn(this)

            searchEventChannel.receiveAsFlow().debounce(1000).onEach {
                emitEffect(ManageSubscriptionsMviModel.Effect.BackToTop)
                refresh()
            }.launchIn(this)
            if (uiState.value.communities.isEmpty()) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: ManageSubscriptionsMviModel.Intent) {
        when (intent) {
            ManageSubscriptionsMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            ManageSubscriptionsMviModel.Intent.Refresh -> screenModelScope.launch { refresh() }
            is ManageSubscriptionsMviModel.Intent.Unsubscribe -> {
                uiState.value.communities.firstOrNull { it.id == intent.id }?.also { community ->
                    unsubscribe(community)
                }
            }

            is ManageSubscriptionsMviModel.Intent.DeleteMultiCommunity -> {
                uiState.value.multiCommunities.firstOrNull {
                    (it.id ?: 0L) == intent.id
                }?.also { community ->
                    deleteMultiCommunity(community)
                }
            }

            is ManageSubscriptionsMviModel.Intent.ToggleFavorite -> {
                uiState.value.communities.firstOrNull { it.id == intent.id }?.also { community ->
                    toggleFavorite(community)
                }
            }

            is ManageSubscriptionsMviModel.Intent.SetSearch -> updateSearchText(intent.value)

            ManageSubscriptionsMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                loadNextPage()
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        updateState {
            it.copy(
                refreshing = true,
                initial = initial,
                canFetchMore = true,
            )
        }
        currentPage = 1
        val accountId = accountRepository.getActive()?.id ?: 0L
        val multiCommunitites =
            multiCommunityRepository.getAll(accountId)
                .let {
                    val searchText = uiState.value.searchText
                    if (searchText.isNotEmpty()) {
                        it.filter { c ->
                            c.name.contains(searchText, ignoreCase = true)
                        }
                    } else {
                        it
                    }
                }
                .sortedBy { it.name }

        updateState {
            it.copy(
                multiCommunities = multiCommunitites,
            )
        }
        loadNextPage()
    }

    private fun unsubscribe(community: CommunityModel) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value
            communityRepository.unsubscribe(
                auth = auth,
                id = community.id,
            )
            updateState {
                it.copy(communities = it.communities.filter { c -> c.id != community.id })
            }
        }
    }

    private fun deleteMultiCommunity(community: MultiCommunityModel) {
        screenModelScope.launch {
            multiCommunityRepository.delete(community)
            updateState {
                val newCommunities = it.multiCommunities.filter { c -> c.id != community.id }
                it.copy(multiCommunities = newCommunities)
            }
        }
    }

    private fun handleMultiCommunityCreated(community: MultiCommunityModel) {
        screenModelScope.launch {
            val oldCommunities = uiState.value.multiCommunities
            val newCommunities =
                if (oldCommunities.any { it.id == community.id }) {
                    oldCommunities.map {
                        if (it.id == community.id) {
                            community
                        } else {
                            it
                        }
                    }
                } else {
                    oldCommunities + community
                }.sortedBy { it.name }
            updateState { it.copy(multiCommunities = newCommunities) }
        }
    }

    private fun toggleFavorite(community: CommunityModel) {
        val communityId = community.id
        screenModelScope.launch {
            val accountId = accountRepository.getActive()?.id ?: 0L
            val newValue = !community.favorite
            if (newValue) {
                val model = FavoriteCommunityModel(communityId = communityId)
                favoriteCommunityRepository.create(model, accountId)
            } else {
                favoriteCommunityRepository.getBy(accountId, communityId)?.also { toDelete ->
                    favoriteCommunityRepository.delete(accountId, toDelete)
                }
            }
            val newCommunity = community.copy(favorite = newValue)
            handleCommunityUpdate(newCommunity)
            emitEffect(ManageSubscriptionsMviModel.Effect.Success)
        }
    }

    private fun handleCommunityUpdate(community: CommunityModel) {
        screenModelScope.launch {
            updateState {
                it.copy(
                    communities =
                        it.communities.map { c ->
                            if (c.id == community.id) {
                                community
                            } else {
                                c
                            }
                        },
                )
            }
        }
    }

    private fun updateSearchText(value: String) {
        screenModelScope.launch {
            updateState { it.copy(searchText = value) }
            searchEventChannel.send(Unit)
        }
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }
        val accountId = accountRepository.getActive()?.id
        val auth = identityRepository.authToken.value
        val searchText = uiState.value.searchText
        val favoriteCommunityIds =
            favoriteCommunityRepository.getAll(accountId).map { it.communityId }
        val itemsToAdd = communityRepository.getSubscribed(
            auth = auth,
            page = currentPage,
            query = searchText,
        ).map { community ->
            community.copy(favorite = community.id in favoriteCommunityIds)
        }.sortedBy { it.name }.let {
            val favorites = it.filter { e -> e.favorite }
            val res = it - favorites.toSet()
            favorites + res
        }
        if (itemsToAdd.isNotEmpty()) {
            currentPage++
        }
        updateState {
            it.copy(
                refreshing = false,
                communities = if (currentState.refreshing) {
                    itemsToAdd
                } else {
                    currentState.communities + itemsToAdd
                },
                canFetchMore = itemsToAdd.isNotEmpty(),
                loading = false,
                initial = false,
            )
        }
    }
}
