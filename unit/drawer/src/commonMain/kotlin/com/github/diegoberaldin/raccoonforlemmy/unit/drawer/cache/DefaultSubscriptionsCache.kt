package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.cache

import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommunityPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommunityPaginationSpecification
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal class DefaultSubscriptionsCache(
    private val identityRepository: IdentityRepository,
    private val communityPaginationManager: CommunityPaginationManager,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SubscriptionsCache {
    override val state = MutableStateFlow<SubscriptionsCacheState>(SubscriptionsCacheState.Loading)

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    override suspend fun initialize() {
        identityRepository.isLogged
            .onEach {
                refresh()
            }.launchIn(scope)
    }

    private suspend fun refresh() {
        communityPaginationManager.reset(CommunityPaginationSpecification.Subscribed())
        val res = communityPaginationManager.fetchAll()
        state.update {
            SubscriptionsCacheState.Loaded(
                communities = res,
            )
        }
    }
}
