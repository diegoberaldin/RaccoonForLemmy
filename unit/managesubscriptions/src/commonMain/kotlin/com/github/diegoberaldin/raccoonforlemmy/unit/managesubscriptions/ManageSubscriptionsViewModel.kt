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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
        }
        if (uiState.value.communities.isEmpty()) {
            refresh()
        }
    }

    override fun reduce(intent: ManageSubscriptionsMviModel.Intent) {
        when (intent) {
            ManageSubscriptionsMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            ManageSubscriptionsMviModel.Intent.Refresh -> refresh()
            is ManageSubscriptionsMviModel.Intent.Unsubscribe -> {
                uiState.value.communities.firstOrNull { it.id == intent.id }?.also { community ->
                    handleUnsubscription(community)
                }
            }

            is ManageSubscriptionsMviModel.Intent.DeleteMultiCommunity -> {
                uiState.value.multiCommunities.firstOrNull() {
                    (it.id ?: 0).toInt() == intent.id
                }?.also { community ->
                    deleteMultiCommunity(community)
                }
            }

            is ManageSubscriptionsMviModel.Intent.ToggleFavorite -> {
                uiState.value.communities.firstOrNull { it.id == intent.id }?.also { community ->
                    toggleFavorite(community)
                }
            }
        }
    }

    private fun refresh() {
        if (uiState.value.refreshing) {
            return
        }
        updateState { it.copy(refreshing = true) }
        screenModelScope.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            val accountId = accountRepository.getActive()?.id ?: 0L
            val favoriteCommunityIds =
                favoriteCommunityRepository.getAll(accountId).map { it.communityId }
            val communities = communityRepository.getSubscribed(auth)
                .map { community ->
                    community.copy(favorite = community.id in favoriteCommunityIds)
                }
                .sortedBy { it.name }
            val multiCommunitites = multiCommunityRepository.getAll(accountId).sortedBy { it.name }

            updateState {
                it.copy(
                    refreshing = false,
                    initial = false,
                    communities = communities,
                    multiCommunities = multiCommunitites,
                )
            }
        }
    }

    private fun handleUnsubscription(community: CommunityModel) {
        screenModelScope.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            communityRepository.unsubscribe(
                auth = auth, id = community.id
            )
            updateState {
                it.copy(communities = it.communities.filter { c -> c.id != community.id })
            }
        }
    }

    private fun deleteMultiCommunity(community: MultiCommunityModel) {
        screenModelScope.launch(Dispatchers.IO) {
            multiCommunityRepository.delete(community)
            updateState {
                val newCommunities = it.multiCommunities.filter { c -> c.id != community.id }
                it.copy(multiCommunities = newCommunities)
            }
        }
    }

    private fun handleMultiCommunityCreated(community: MultiCommunityModel) {
        val oldCommunities = uiState.value.multiCommunities
        val newCommunities = if (oldCommunities.any { it.id == community.id }) {
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

    private fun toggleFavorite(community: CommunityModel) {
        val communityId = community.id
        screenModelScope.launch(Dispatchers.IO) {
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
        }
    }

    private fun handleCommunityUpdate(community: CommunityModel) {
        updateState {
            it.copy(
                communities = it.communities.map { c ->
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