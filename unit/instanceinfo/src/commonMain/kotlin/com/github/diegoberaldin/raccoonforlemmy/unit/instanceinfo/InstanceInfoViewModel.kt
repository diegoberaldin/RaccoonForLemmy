package com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo

import cafe.adriel.voyager.core.model.screenModelScope
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommunityPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommunityPaginationSpecification
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InstanceInfoViewModel(
    private val url: String,
    private val siteRepository: SiteRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val getSortTypesUseCase: GetSortTypesUseCase,
    private val communityPaginationManager: CommunityPaginationManager,
) : DefaultMviModel<InstanceInfoMviModel.Intent, InstanceInfoMviModel.UiState, InstanceInfoMviModel.Effect>(
        initialState = InstanceInfoMviModel.UiState(),
    ),
    InstanceInfoMviModel {
    init {
        screenModelScope.launch {
            settingsRepository.currentSettings
                .onEach { settings ->
                    updateState {
                        it.copy(
                            autoLoadImages = settings.autoLoadImages,
                            preferNicknames = settings.preferUserNicknames,
                        )
                    }
                }.launchIn(this)
            notificationCenter
                .subscribe(NotificationCenterEvent.ChangeSortType::class)
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
            if (uiState.value.initial) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: InstanceInfoMviModel.Intent) {
        when (intent) {
            InstanceInfoMviModel.Intent.LoadNextPage -> loadNextPage()
            InstanceInfoMviModel.Intent.Refresh ->
                screenModelScope.launch {
                    refresh()
                }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        val instance = url.replace("https://", "")
        communityPaginationManager.reset(
            CommunityPaginationSpecification.Instance(
                otherInstance = instance,
                sortType = uiState.value.sortType,
            ),
        )
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = !initial,
                loading = false,
                initial = initial,
            )
        }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            screenModelScope.launch {
                updateState { it.copy(refreshing = false) }
            }
            return
        }

        screenModelScope.launch {
            updateState { it.copy(loading = true) }
            val itemsToAdd = communityPaginationManager.loadNextPage()
            updateState {
                it.copy(
                    communities = itemsToAdd,
                    loading = false,
                    canFetchMore = communityPaginationManager.canFetchMore,
                    refreshing = false,
                )
            }
        }
    }

    private fun changeSortType(value: SortType) {
        screenModelScope.launch {
            updateState { it.copy(sortType = value) }
            emitEffect(InstanceInfoMviModel.Effect.BackToTop)
            refresh()
        }
    }
}
