package com.github.diegoberaldin.raccoonforlemmy.unit.modlog

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.ModlogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ModlogViewModel(
    private val mvi: DefaultMviModel<ModlogMviModel.Intent, ModlogMviModel.UiState, ModlogMviModel.Effect>,
    private val communityId: Int,
    private val themeRepository: ThemeRepository,
    private val identityRepository: IdentityRepository,
    private val modlogRepository: ModlogRepository,
    private val settingsRepository: SettingsRepository,
) : ModlogMviModel,
    MviModel<ModlogMviModel.Intent, ModlogMviModel.UiState, ModlogMviModel.Effect> by mvi {

    private var currentPage: Int = 1

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
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
            ModlogMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }
        }
    }

    private fun refresh(initial: Boolean = false) {
        currentPage = 1
        mvi.updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
            )
        }
        mvi.scope?.launch {
            loadNextPage()
        }
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val refreshing = currentState.refreshing
            val itemList = modlogRepository.getItems(
                auth = auth,
                communityId = communityId,
                page = currentPage,
            )
            val itemsToAdd = itemList.orEmpty()
            mvi.updateState {
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