package com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SelectCommunityViewModel(
    private val mvi: DefaultMviModel<SelectCommunityMviModel.Intent, SelectCommunityMviModel.UiState, SelectCommunityMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val settingsRepository: SettingsRepository,
) : SelectCommunityMviModel,
    MviModel<SelectCommunityMviModel.Intent, SelectCommunityMviModel.UiState, SelectCommunityMviModel.Effect> by mvi {

    private var communities: List<CommunityModel> = emptyList()
    private var debounceJob: Job? = null

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
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
        debounceJob?.cancel()
        mvi.updateState { it.copy(searchText = value) }
        debounceJob = mvi.scope?.launch(Dispatchers.IO) {
            delay(1_000)
            mvi.updateState {
                val filtered = filterCommunities()
                it.copy(communities = filtered)
            }
        }
    }

    private fun populate() {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            communities = communityRepository.getSubscribed(auth).sortedBy { it.name }
            mvi.updateState {
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