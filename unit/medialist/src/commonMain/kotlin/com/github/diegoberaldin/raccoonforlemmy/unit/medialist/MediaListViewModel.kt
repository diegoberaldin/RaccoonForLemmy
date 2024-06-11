package com.github.diegoberaldin.raccoonforlemmy.unit.medialist

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MediaModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.MediaRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MediaListViewModel(
    private val identityRepository: IdentityRepository,
    private val mediaRepository: MediaRepository,
    private val settingsRepository: SettingsRepository,
    private val themeRepository: ThemeRepository,
) : MediaListMviModel,
    DefaultMviModel<MediaListMviModel.Intent, MediaListMviModel.State, MediaListMviModel.Effect>(
        initialState = MediaListMviModel.State(),
    ) {
    private var currentPage = 1

    init {
        screenModelScope.launch {
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        fullHeightImages = settings.fullHeightImages,
                        fullWidthImages = settings.fullWidthImages,
                    )
                }
            }.launchIn(this)

            if (uiState.value.initial) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: MediaListMviModel.Intent) {
        when (intent) {
            MediaListMviModel.Intent.Refresh ->
                screenModelScope.launch {
                    refresh()
                }

            MediaListMviModel.Intent.LoadNextPage ->
                screenModelScope.launch {
                    loadNextPage()
                }

            is MediaListMviModel.Intent.Delete -> deleteMedia(intent.media)
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        updateState {
            it.copy(
                initial = initial,
                canFetchMore = true,
                refreshing = !initial,
                loading = false,
            )
        }
        loadNextPage()
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }
        updateState { it.copy(loading = true) }

        val auth = identityRepository.authToken.value
        val media =
            mediaRepository.getAll(
                auth = auth,
                page = currentPage,
            )
        val canFetchMore = media.isNotEmpty()
        updateState {
            it.copy(
                media = media,
                loading = false,
                canFetchMore = canFetchMore,
                refreshing = false,
                initial = false,
            )
        }
    }

    private fun deleteMedia(media: MediaModel) {
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value
                mediaRepository.delete(
                    auth = auth,
                    media = media,
                )
                updateState { it.copy(media = it.media.filter { m -> m.alias != media.alias }) }
            } catch (e: Exception) {
                emitEffect(MediaListMviModel.Effect.Failure(e.message))
            }
        }
    }
}
