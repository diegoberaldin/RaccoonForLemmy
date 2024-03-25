package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.MultiCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ValidationError
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
class MultiCommunityEditorViewModel(
    private val communityId: Int?,
    private val identityRepository: IdentityRepository,
    private val communityRepository: CommunityRepository,
    private val multiCommunityRepository: MultiCommunityRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
) : MultiCommunityEditorMviModel,
    DefaultMviModel<MultiCommunityEditorMviModel.Intent, MultiCommunityEditorMviModel.UiState, MultiCommunityEditorMviModel.Effect>(
        initialState = MultiCommunityEditorMviModel.UiState()
    ) {

    private var communities: List<Pair<CommunityModel, Boolean>> = emptyList()
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

    override fun reduce(intent: MultiCommunityEditorMviModel.Intent) {
        when (intent) {
            is MultiCommunityEditorMviModel.Intent.SelectImage -> selectImage(intent.index)
            is MultiCommunityEditorMviModel.Intent.SetName -> updateState { it.copy(name = intent.value) }
            is MultiCommunityEditorMviModel.Intent.ToggleCommunity -> toggleCommunity(intent.id)
            is MultiCommunityEditorMviModel.Intent.SetSearch -> setSearch(intent.value)
            MultiCommunityEditorMviModel.Intent.Submit -> submit()
        }
    }

    private fun populate() {
        screenModelScope.launch(Dispatchers.IO) {
            val editedCommunity = communityId?.toLong()?.let {
                multiCommunityRepository.getById(it)
            }
            val auth = identityRepository.authToken.value
            communities = communityRepository.getSubscribed(auth).sortedBy { it.name }.map { c ->
                c to (editedCommunity?.communityIds?.contains(c.id) == true)
            }
            updateState {
                val newCommunities = communities
                val availableIcons = newCommunities.filter { i -> i.second }.mapNotNull { i -> i.first.icon }
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
        updateState { it.copy(searchText = value) }
        screenModelScope.launch {
            searchEventChannel.send(Unit)
        }
    }

    private fun filterCommunities(): List<Pair<CommunityModel, Boolean>> {
        val searchText = uiState.value.searchText
        val res = if (searchText.isNotEmpty()) {
            communities.filter { it.first.name.contains(other = searchText, ignoreCase = true) }
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
        updateState { it.copy(icon = image) }
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
        updateState { state ->
            state.copy(
                communities = filtered,
                availableIcons = availableIcons,
            )
        }
    }

    private fun submit() {
        updateState { it.copy(nameError = null) }
        val currentState = uiState.value
        var valid = true
        val name = currentState.name
        if (name.isEmpty()) {
            updateState { it.copy(nameError = ValidationError.MissingField) }
            valid = false
        }
        if (!valid) {
            return
        }

        screenModelScope.launch(Dispatchers.IO) {
            val icon = currentState.icon
            val communityIds = currentState.communities.filter { it.second }.map { it.first.id }
            val editedCommunity = communityId?.toLong()?.let {
                multiCommunityRepository.getById(it)
            }
            val multiCommunity = editedCommunity?.copy(
                name = name,
                icon = icon,
                communityIds = communityIds,
            ) ?: MultiCommunityModel(
                name = name,
                icon = icon,
                communityIds = communityIds,
            )
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
            emitEffect(MultiCommunityEditorMviModel.Effect.Close)
        }
    }
}