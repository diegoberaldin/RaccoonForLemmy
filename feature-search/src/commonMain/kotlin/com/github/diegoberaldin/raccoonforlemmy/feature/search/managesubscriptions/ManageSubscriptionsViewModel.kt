package com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
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
    private val mvi: DefaultMviModel<ManageSubscriptionsMviModel.Intent, ManageSubscriptionsMviModel.UiState, ManageSubscriptionsMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val accountRepository: AccountRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val settingsRepository: SettingsRepository,
    private val hapticFeedback: HapticFeedback,
    private val notificationCenter: NotificationCenter,
) : ManageSubscriptionsMviModel,
    MviModel<ManageSubscriptionsMviModel.Intent, ManageSubscriptionsMviModel.UiState, ManageSubscriptionsMviModel.Effect> by mvi {

    init {
        notificationCenter.addObserver(
            { evt ->
                (evt as? MultiCommunityModel)?.also {
                    handleMultiCommunityCreated(it)
                }
            },
            this::class.simpleName.orEmpty(),
            NotificationCenterContractKeys.MultiCommunityCreated
        )
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
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
            is ManageSubscriptionsMviModel.Intent.Unsubscribe -> handleUnsubscription(
                community = uiState.value.communities.first { it.id == intent.id },
            )

            is ManageSubscriptionsMviModel.Intent.DeleteMultiCommunity -> deleteMultiCommunity(
                community = uiState.value.multiCommunities.first {
                    (it.id ?: 0).toInt() == intent.id
                },
            )
        }
    }

    private fun refresh() {
        if (uiState.value.refreshing) {
            return
        }
        mvi.updateState { it.copy(refreshing = true) }
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            val communities = communityRepository.getSubscribed(auth).sortedBy { it.name }
            val accountId = accountRepository.getActive()?.id ?: 0L
            val multiCommunitites = multiCommunityRepository.getAll(accountId).sortedBy { it.name }

            mvi.updateState {
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
        mvi.scope?.launch {
            val auth = identityRepository.authToken.value
            communityRepository.unsubscribe(
                auth = auth, id = community.id
            )
            mvi.updateState {
                it.copy(communities = it.communities.filter { c -> c.id != community.id })
            }
        }
    }

    private fun deleteMultiCommunity(community: MultiCommunityModel) {
        mvi.scope?.launch(Dispatchers.IO) {
            multiCommunityRepository.delete(community)
            mvi.updateState {
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
        mvi.updateState { it.copy(multiCommunities = newCommunities) }
    }
}