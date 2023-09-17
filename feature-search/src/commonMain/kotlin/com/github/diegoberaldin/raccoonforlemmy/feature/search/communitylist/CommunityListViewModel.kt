package com.github.diegoberaldin.raccoonforlemmy.feature.search.communitylist

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.KeyStoreKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CommunityListViewModel(
    private val mvi: DefaultMviModel<CommunityListMviModel.Intent, CommunityListMviModel.UiState, CommunityListMviModel.Effect>,
    private val apiConfigRepository: ApiConfigurationRepository,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val keyStore: TemporaryKeyStore,
    private val notificationCenter: NotificationCenter,
) : ScreenModel,
    MviModel<CommunityListMviModel.Intent, CommunityListMviModel.UiState, CommunityListMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    private var debounceJob: Job? = null

    init {
        notificationCenter.addObserver({
            handleLogout()
        }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.Logout)
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

    override fun onStarted() {
        mvi.onStarted()
        mvi.updateState {
            it.copy(
                instance = apiConfigRepository.getInstance(),
            )
        }
        mvi.scope?.launch(Dispatchers.Main) {
            identityRepository.authToken.map { !it.isNullOrEmpty() }.onEach { isLogged ->
                mvi.updateState {
                    it.copy(isLogged = isLogged)
                }
            }.launchIn(this)
        }

        if (mvi.uiState.value.communities.isEmpty()) {
            mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }
        }
    }

    override fun reduce(intent: CommunityListMviModel.Intent) {
        when (intent) {
            CommunityListMviModel.Intent.LoadNextPage -> {
                mvi.scope?.launch(Dispatchers.IO) {
                    loadNextPage()
                }
            }

            CommunityListMviModel.Intent.Refresh -> {
                mvi.scope?.launch(Dispatchers.IO) {
                    refresh()
                }
            }

            is CommunityListMviModel.Intent.SetSearch -> setSearch(intent.value)
            is CommunityListMviModel.Intent.SetListingType -> changeListingType(intent.value)
            is CommunityListMviModel.Intent.SetSortType -> changeSortType(intent.value)
        }
    }

    private fun setSearch(value: String) {
        debounceJob?.cancel()
        mvi.updateState { it.copy(searchText = value) }
        debounceJob = mvi.scope?.launch(Dispatchers.IO) {
            delay(1_000)
            refresh()
        }
    }

    private fun changeListingType(value: ListingType) {
        mvi.updateState { it.copy(listingType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            refresh()
        }
    }

    private fun changeSortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            refresh()
        }
    }

    private suspend fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
    }

    private suspend fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }
        mvi.updateState { it.copy(loading = true) }
        val searchText = mvi.uiState.value.searchText
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val listingType = currentState.listingType
        val sortType = currentState.sortType
        val inclueNsfw = keyStore[KeyStoreKeys.IncludeNsfw, true]
        val items = communityRepository.getAll(
            query = searchText,
            auth = auth,
            page = currentPage,
            listingType = listingType,
            sortType = sortType,
        )
        currentPage++
        val canFetchMore = items.size >= PostsRepository.DEFAULT_PAGE_SIZE
        mvi.updateState {
            val newItems = if (refreshing) {
                items
            } else {
                it.communities + items
            }.filter { community ->
                if (inclueNsfw) {
                    true
                } else {
                    !community.nsfw
                }
            }
            it.copy(
                communities = newItems,
                loading = false,
                canFetchMore = canFetchMore,
                refreshing = false,
            )
        }
    }

    private fun handleLogout() {
        currentPage = 1
        mvi.updateState {
            it.copy(
                listingType = ListingType.Local,
                communities = emptyList(),
            )
        }
    }
}
