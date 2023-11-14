package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.editor

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MultiCommunityEditorViewModel(
    private val mvi: DefaultMviModel<MultiCommunityEditorMviModel.Intent, MultiCommunityEditorMviModel.UiState, MultiCommunityEditorMviModel.Effect>,
    private val editedCommunity: MultiCommunityModel? = null,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
) : MultiCommunityEditorMviModel,
    MviModel<MultiCommunityEditorMviModel.Intent, MultiCommunityEditorMviModel.UiState, MultiCommunityEditorMviModel.Effect> by mvi {

    private var communities: List<Pair<CommunityModel, Boolean>> = emptyList()
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

    override fun reduce(intent: MultiCommunityEditorMviModel.Intent) {
        when (intent) {
            is MultiCommunityEditorMviModel.Intent.SelectImage -> selectImage(intent.index)
            is MultiCommunityEditorMviModel.Intent.SetName -> mvi.updateState { it.copy(name = intent.value) }
            is MultiCommunityEditorMviModel.Intent.ToggleCommunity -> toggleCommunity(intent.id)
            is MultiCommunityEditorMviModel.Intent.SetSearch -> setSearch(intent.value)
            MultiCommunityEditorMviModel.Intent.Submit -> submit()
        }
    }

    private fun populate() {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            communities = communityRepository.getSubscribed(auth).sortedBy { it.name }.map { c ->
                c to (editedCommunity?.communityIds?.contains(c.id) == true)
            }
            mvi.updateState {
                val newCommunities = communities
                val availableIcons =
                    newCommunities.filter { i -> i.second }.mapNotNull { i -> i.first.icon }
                it.copy(
                    communities = newCommunities,
                    name = editedCommunity?.name.orEmpty(),
                    icon = editedCommunity?.icon,
                    availableIcons = availableIcons,
                )
            }
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

    private fun filterCommunities(): List<Pair<CommunityModel, Boolean>> {
        val searchText = uiState.value.searchText
        val res = if (searchText.isNotEmpty()) {
            communities.filter { it.first.name.contains(searchText) }
        } else {
            communities
        }
        return res
    }

    private fun selectImage(index: Int?) {
        val image = if (index == null) {
            null
        } else {
            uiState.value.availableIcons[index]
        }
        mvi.updateState { it.copy(icon = image) }
    }

    private fun toggleCommunity(communityId: Int) {
        val newCommunities = communities.map { item ->
            if (item.first.id == communityId) {
                item.first to !item.second
            } else {
                item
            }
        }
        val availableIcons = newCommunities.filter { i ->
            i.second
        }.mapNotNull { i ->
            i.first.icon
        }
        communities = newCommunities
        val filtered = filterCommunities()
        mvi.updateState { state ->
            state.copy(
                communities = filtered,
                availableIcons = availableIcons,
            )
        }
    }

    private fun submit() {
        mvi.updateState { it.copy(nameError = null) }
        val currentState = uiState.value
        var valid = true
        val name = currentState.name
        if (name.isEmpty()) {
            mvi.updateState { it.copy(nameError = MR.strings.message_missing_field.desc()) }
            valid = false
        }
        if (!valid) {
            return
        }

        val icon = currentState.icon
        val communityIds = currentState.communities.filter { it.second }.map { it.first.id }
        val multiCommunity = editedCommunity?.copy(
            name = name,
            icon = icon,
            communityIds = communityIds,
        ) ?: MultiCommunityModel(
            name = name,
            icon = icon,
            communityIds = communityIds,
        )

        mvi.scope?.launch(Dispatchers.IO) {
            val accountId = accountRepository.getActive()?.id ?: return@launch
            if (multiCommunity.id == null) {
                val id = multiCommunityRepository.create(multiCommunity, accountId)
                notificationCenter.send(
                    NotificationCenterEvent.MultiCommunityCreated(multiCommunity.copy(id = id))
                )
            } else {
                multiCommunityRepository.update(multiCommunity)
                notificationCenter.send(NotificationCenterEvent.MultiCommunityCreated(multiCommunity))
            }
            mvi.emitEffect(MultiCommunityEditorMviModel.Effect.Close)
        }
    }
}