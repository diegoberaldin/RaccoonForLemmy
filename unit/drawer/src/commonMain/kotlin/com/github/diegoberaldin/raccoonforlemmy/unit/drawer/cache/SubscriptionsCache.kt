package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.cache

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import kotlinx.coroutines.flow.StateFlow

sealed interface SubscriptionsCacheState {
    data object Loading : SubscriptionsCacheState

    data class Loaded(
        val communities: List<CommunityModel>,
    ) : SubscriptionsCacheState
}

interface SubscriptionsCache {
    val state: StateFlow<SubscriptionsCacheState>

    suspend fun initialize()
}
