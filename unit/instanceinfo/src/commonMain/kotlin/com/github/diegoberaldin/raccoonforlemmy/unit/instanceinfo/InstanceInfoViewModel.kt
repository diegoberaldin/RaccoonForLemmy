package com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InstanceInfoViewModel(
    private val url: String,
    private val siteRepository: SiteRepository,
    private val communityRepository: CommunityRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val getSortTypesUseCase: GetSortTypesUseCase,
) : InstanceInfoMviModel,
    DefaultMviModel<InstanceInfoMviModel.Intent, InstanceInfoMviModel.UiState, InstanceInfoMviModel.Effect>(
        initialState = InstanceInfoMviModel.UiState()
    ) {

    private var currentPage = 1

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
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == "instanceInfo") {
                        changeSortType(evt.value)
                    }
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
        }
    }

    private fun refresh() {
        currentPage = 1
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                loading = false,
            )
        }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        screenModelScope.launch {
            updateState { it.copy(loading = true) }
            val refreshing = currentState.refreshing
            val instance = url.replace("https://", "")
            val itemList = communityRepository.getList(
                instance = instance,
                page = currentPage,
                sortType = currentState.sortType,
                limit = 50,
            ).let {
                if (refreshing) {
                    it
                } else {
                    // prevents accidental duplication
                    it.filter { c1 ->
                        currentState.communities.none { c2 -> c1.id == c2.id }
                    }
                }
            }
            if (itemList.isNotEmpty()) {
                currentPage++
            }
            val itemsToAdd = itemList.filter { e ->
                e.instanceUrl == url
            }
            updateState {
                it.copy(
                    communities = if (refreshing) {
                        itemsToAdd
                    } else {
                        it.communities + itemsToAdd
                    },
                    loading = false,
                    canFetchMore = itemList.isNotEmpty(),
                    refreshing = false,
                )
            }
        }
    }

    private fun changeSortType(value: SortType) {
        updateState { it.copy(sortType = value) }
        screenModelScope.launch {
            emitEffect(InstanceInfoMviModel.Effect.BackToTop)
            refresh()
        }
    }
}
