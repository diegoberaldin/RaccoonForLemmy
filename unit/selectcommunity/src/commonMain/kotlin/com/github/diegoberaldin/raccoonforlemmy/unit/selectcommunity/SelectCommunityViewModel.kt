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

@OptIn(FlowPreview::class)
class SelectCommunityViewModel(
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val settingsRepository: SettingsRepository,
) : SelectCommunityMviModel,
    DefaultMviModel<SelectCommunityMviModel.Intent, SelectCommunityMviModel.UiState, SelectCommunityMviModel.Effect>(
        initialState = SelectCommunityMviModel.UiState(),
    ) {

        private var communities: List<CommunityModel> = emptyList()
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

                searchEventChannel.receiveAsFlow().debounce(1000).onEach {
                    updateState {
                        val filtered = filterCommunities()
                        it.copy(communities = filtered)
                    }
                }.launchIn(this)
            }
            if (communities.isEmpty()) {
                populate()
            }
        }

        override fun reduce(intent: SelectCommunityMviModel.Intent) {
            when (intent) {
                is SelectCommunityMviModel.Intent.SetSearch -> setSearch(intent.value)
            }
        }

        private fun setSearch(value: String) {
            updateState { it.copy(searchText = value) }
            screenModelScope.launch {
                searchEventChannel.send(Unit)
            }
        }

        private fun populate() {
            screenModelScope.launch(Dispatchers.IO) {
                val auth = identityRepository.authToken.value
                communities = communityRepository.getSubscribed(auth).sortedBy { it.name }
                updateState {
                    it.copy(
                        initial = false,
                        communities = communities,
                    )
                }
            }
        }

        private fun filterCommunities(): List<CommunityModel> {
            val searchText = uiState.value.searchText
            val res = if (searchText.isNotEmpty()) {
                communities.filter { it.name.contains(searchText) }
            } else {
                communities
            }
            return res
        }
    }
