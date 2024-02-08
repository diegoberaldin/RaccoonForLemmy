package com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InstanceInfoViewModel(
    private val url: String,
    private val siteRepository: SiteRepository,
    private val communityRepository: CommunityRepository,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val getSortTypesUseCase: GetSortTypesUseCase,
) : InstanceInfoMviModel,
    DefaultMviModel<InstanceInfoMviModel.Intent, InstanceInfoMviModel.UiState, InstanceInfoMviModel.Effect>(
        initialState = InstanceInfoMviModel.UiState()
    ) {

    private var currentPage = 1

    override fun onStarted() {
        super.onStarted()
        scope?.launch {
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    changeSortType(evt.value)
                }.launchIn(this)

            val metadata = siteRepository.getMetadata(url)
            val sortTypes = getSortTypesUseCase.getTypesForCommunities()
            if (metadata != null) {
                metadata.title
                updateState {
                    it.copy(
                        title = metadata.title,
                        description = metadata.description,
                        availableSortTypes = sortTypes,
                    )
                }
            }
        }

        if (uiState.value.communities.isEmpty()) {
            refresh()
        }
    }

    override fun reduce(intent: InstanceInfoMviModel.Intent) {
        when (intent) {
            InstanceInfoMviModel.Intent.LoadNextPage -> loadNextPage()
            InstanceInfoMviModel.Intent.Refresh -> refresh()
            is InstanceInfoMviModel.Intent.ChangeSortType -> changeSortType(intent.value)
        }
    }

    private fun refresh() {
        currentPage = 1
        updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        scope?.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val refreshing = currentState.refreshing
            val instance = url.replace("https://", "")
            val itemList = communityRepository.getAll(
                auth = auth,
                instance = instance,
                page = currentPage,
                listingType = ListingType.Local,
                sortType = currentState.sortType,
                resultType = SearchResultType.Communities,
                limit = 50,
            )?.filterIsInstance<SearchResult.Community>()
                ?.let {
                    if (refreshing) {
                        it
                    } else {
                        // prevents accidental duplication
                        it.filter { c1 ->
                            currentState.communities.none { c2 -> c1.model.id == c2.id }
                        }
                    }
                }
            if (!itemList.isNullOrEmpty()) {
                currentPage++
            }
            val itemsToAdd = itemList.orEmpty().filter { e ->
                e.model.instanceUrl == url
            }.map { it.model }
            updateState {
                it.copy(
                    communities = if (refreshing) {
                        itemsToAdd
                    } else {
                        it.communities + itemsToAdd
                    },
                    loading = false,
                    canFetchMore = itemList?.isEmpty() != true,
                    refreshing = false,
                )
            }
        }
    }

    private fun changeSortType(value: SortType) {
        updateState { it.copy(sortType = value) }
        scope?.launch(Dispatchers.IO) {
            emitEffect(InstanceInfoMviModel.Effect.BackToTop)
            refresh()
        }
    }
}
