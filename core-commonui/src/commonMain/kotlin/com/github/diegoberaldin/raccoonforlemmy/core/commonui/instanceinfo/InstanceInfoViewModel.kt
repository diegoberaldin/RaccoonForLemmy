package com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InstanceInfoViewModel(
    private val mvi: DefaultMviModel<InstanceInfoMviModel.Intent, InstanceInfoMviModel.UiState, InstanceInfoMviModel.Effect>,
    private val url: String,
    private val siteRepository: SiteRepository,
    private val communityRepository: CommunityRepository,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
) : InstanceInfoMviModel,
    MviModel<InstanceInfoMviModel.Intent, InstanceInfoMviModel.UiState, InstanceInfoMviModel.Effect> by mvi {

    private var currentPage = 1

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch(Dispatchers.IO) {
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
            }.launchIn(this)

            val metadata = siteRepository.getMetadata(url)
            if (metadata != null) {
                metadata.title
                mvi.updateState {
                    it.copy(
                        title = metadata.title,
                        description = metadata.description,
                    )
                }
            }
        }

        if (mvi.uiState.value.communities.isEmpty()) {
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
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
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
            mvi.updateState {
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
        mvi.updateState { it.copy(sortType = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.emitEffect(InstanceInfoMviModel.Effect.BackToTop)
            refresh()
        }
    }
}
