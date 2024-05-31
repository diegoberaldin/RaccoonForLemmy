package com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SelectCommunityViewModel(
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val settingsRepository: SettingsRepository,
) : SelectCommunityMviModel,
    DefaultMviModel<SelectCommunityMviModel.Intent, SelectCommunityMviModel.UiState, SelectCommunityMviModel.Effect>(
        initialState = SelectCommunityMviModel.UiState(),
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

            @OptIn(FlowPreview::class)
            searchEventChannel.receiveAsFlow().debounce(1000).onEach {
                refresh()
            }.launchIn(this)

            if (uiState.value.initial) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: SelectCommunityMviModel.Intent) {
        when (intent) {
            is SelectCommunityMviModel.Intent.SetSearch -> setSearch(intent.value)
            SelectCommunityMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                loadNextPage()
            }
        }
    }

    private fun setSearch(value: String) {
        screenModelScope.launch {
            updateState { it.copy(searchText = value) }
            searchEventChannel.send(Unit)
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        updateState {
            it.copy(
                initial = initial,
                canFetchMore = true,
                loading = false,
                refreshing = true,
            )
        }
        loadNextPage()
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            return
        }
        val auth = identityRepository.authToken.value
        val searchText = uiState.value.searchText
        val itemsToAdd = communityRepository.getSubscribed(
            auth = auth,
            page = currentPage,
            query = searchText,
        )
        if (itemsToAdd.isNotEmpty()) {
            currentPage++
        }
        updateState {
            it.copy(
                communities = if (currentState.refreshing) {
                    itemsToAdd
                } else {
                    currentState.communities + itemsToAdd
                },
                canFetchMore = itemsToAdd.isNotEmpty(),
                loading = false,
                initial = false,
                refreshing = false,
            )
        }
    }
}
