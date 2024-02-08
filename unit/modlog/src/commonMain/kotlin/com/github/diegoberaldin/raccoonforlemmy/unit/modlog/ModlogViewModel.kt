package com.github.diegoberaldin.raccoonforlemmy.unit.modlog

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.ModlogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ModlogViewModel(
    private val communityId: Int,
    private val themeRepository: ThemeRepository,
    private val identityRepository: IdentityRepository,
    private val modlogRepository: ModlogRepository,
    private val settingsRepository: SettingsRepository,
) : ModlogMviModel,
    DefaultMviModel<ModlogMviModel.Intent, ModlogMviModel.UiState, ModlogMviModel.Effect>(
        initialState = ModlogMviModel.UiState()
    ) {

    private var currentPage: Int = 1

    override fun onStarted() {
        super.onStarted()
        scope?.launch {
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                    )
                }
            }

            if (uiState.value.items.isEmpty()) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: ModlogMviModel.Intent) {
        when (intent) {
            ModlogMviModel.Intent.Refresh -> refresh()
            ModlogMviModel.Intent.LoadNextPage -> scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }
        }
    }

    private fun refresh(initial: Boolean = false) {
        currentPage = 1
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
            )
        }
        scope?.launch(Dispatchers.IO) {
            loadNextPage()
        }
    }

    private fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        scope?.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val refreshing = currentState.refreshing
            val itemList = modlogRepository.getItems(
                auth = auth,
                communityId = communityId,
                page = currentPage,
            )
            val itemsToAdd = itemList.orEmpty()
            updateState {
                val modlogItems = if (refreshing) {
                    itemsToAdd
                } else {
                    it.items + itemsToAdd
                }
                it.copy(
                    items = modlogItems,
                    loading = false,
                    canFetchMore = itemList?.isEmpty() != true,
                    refreshing = false,
                    initial = false,
                )
            }
            if (!itemList.isNullOrEmpty()) {
                currentPage++
            }
        }
    }
}