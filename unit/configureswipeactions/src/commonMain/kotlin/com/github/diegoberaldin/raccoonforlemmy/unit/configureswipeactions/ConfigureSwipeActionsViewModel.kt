package com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipeDirection
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipeTarget
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ConfigureSwipeActionsViewModel(
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
) : ConfigureSwipeActionsMviModel,
    DefaultMviModel<ConfigureSwipeActionsMviModel.Intent, ConfigureSwipeActionsMviModel.UiState, ConfigureSwipeActionsMviModel.Effect>(
        initialState = ConfigureSwipeActionsMviModel.UiState(),
    ) {
    init {
        screenModelScope.launch {
            notificationCenter.subscribe(NotificationCenterEvent.ActionsOnSwipeSelected::class)
                .onEach { evt ->
                    when (evt.target) {
                        ActionOnSwipeTarget.Posts ->
                            addActionPosts(
                                action = evt.value,
                                direction = evt.direction,
                            )

                        ActionOnSwipeTarget.Comments ->
                            addActionComments(
                                action = evt.value,
                                direction = evt.direction,
                            )

                        ActionOnSwipeTarget.Inbox ->
                            addActionInbox(
                                action = evt.value,
                                direction = evt.direction,
                            )
                    }
                }.launchIn(this)
        }
        refresh()
    }

    override fun reduce(intent: ConfigureSwipeActionsMviModel.Intent) {
        when (intent) {
            is ConfigureSwipeActionsMviModel.Intent.DeleteActionComments ->
                removeActionComments(
                    action = intent.value,
                    direction = intent.direction,
                )

            is ConfigureSwipeActionsMviModel.Intent.DeleteActionInbox ->
                removeActionInbox(
                    action = intent.value,
                    direction = intent.direction,
                )

            is ConfigureSwipeActionsMviModel.Intent.DeleteActionPosts ->
                removeActionPosts(
                    action = intent.value,
                    direction = intent.direction,
                )

            ConfigureSwipeActionsMviModel.Intent.ResetActionsComments -> resetActionsComments()
            ConfigureSwipeActionsMviModel.Intent.ResetActionsInbox -> resetActionsInbox()
            ConfigureSwipeActionsMviModel.Intent.ResetActionsPosts -> resetActionsPosts()
        }
    }

    private fun refresh() {
        val settings = settingsRepository.currentSettings.value
        updateState {
            it.copy(
                actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts,
                actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts,
                actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                actionsOnSwipeToStartInbox = settings.actionsOnSwipeToStartInbox,
                actionsOnSwipeToEndInbox = settings.actionsOnSwipeToEndInbox,
            )
        }
        updateAvailableOptions()
    }

    private fun addActionPosts(
        action: ActionOnSwipe,
        direction: ActionOnSwipeDirection,
    ) {
        screenModelScope.launch {
            val settings = settingsRepository.currentSettings.value
            val accountId = accountRepository.getActive()?.id ?: return@launch
            val newActions =
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        (settings.actionsOnSwipeToStartPosts + action).toSet().toList()
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        (settings.actionsOnSwipeToEndPosts + action).toSet().toList()
                    }
                }
            val newSettings =
                when (direction) {
                    ActionOnSwipeDirection.ToStart ->
                        settings.copy(
                            actionsOnSwipeToStartPosts = newActions,
                        )

                    ActionOnSwipeDirection.ToEnd ->
                        settings.copy(
                            actionsOnSwipeToEndPosts = newActions,
                        )
                }
            settingsRepository.updateSettings(settings = newSettings, accountId = accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            updateState {
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        it.copy(
                            actionsOnSwipeToStartPosts = newActions,
                        )
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        it.copy(
                            actionsOnSwipeToEndPosts = newActions,
                        )
                    }
                }
            }
        }
    }

    private fun removeActionPosts(
        action: ActionOnSwipe,
        direction: ActionOnSwipeDirection,
    ) {
        screenModelScope.launch {
            val settings = settingsRepository.currentSettings.value
            val accountId = accountRepository.getActive()?.id ?: return@launch
            val newActions =
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        (settings.actionsOnSwipeToStartPosts - action).toSet().toList()
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        (settings.actionsOnSwipeToEndPosts - action).toSet().toList()
                    }
                }
            val newSettings =
                when (direction) {
                    ActionOnSwipeDirection.ToStart ->
                        settings.copy(
                            actionsOnSwipeToStartPosts = newActions,
                        )

                    ActionOnSwipeDirection.ToEnd ->
                        settings.copy(
                            actionsOnSwipeToEndPosts = newActions,
                        )
                }
            settingsRepository.updateSettings(settings = newSettings, accountId = accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            updateState {
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        it.copy(
                            actionsOnSwipeToStartPosts = newActions,
                        )
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        it.copy(
                            actionsOnSwipeToEndPosts = newActions,
                        )
                    }
                }
            }
        }
    }

    private fun addActionComments(
        action: ActionOnSwipe,
        direction: ActionOnSwipeDirection,
    ) {
        screenModelScope.launch {
            val settings = settingsRepository.currentSettings.value
            val accountId = accountRepository.getActive()?.id ?: return@launch
            val newActions =
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        (settings.actionsOnSwipeToStartComments + action).toSet().toList()
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        (settings.actionsOnSwipeToEndComments + action).toSet().toList()
                    }
                }
            val newSettings =
                when (direction) {
                    ActionOnSwipeDirection.ToStart ->
                        settings.copy(
                            actionsOnSwipeToStartComments = newActions,
                        )

                    ActionOnSwipeDirection.ToEnd ->
                        settings.copy(
                            actionsOnSwipeToEndComments = newActions,
                        )
                }
            settingsRepository.updateSettings(settings = newSettings, accountId = accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            updateState {
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        it.copy(
                            actionsOnSwipeToStartComments = newActions,
                        )
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        it.copy(
                            actionsOnSwipeToEndComments = newActions,
                        )
                    }
                }
            }
        }
    }

    private fun removeActionComments(
        action: ActionOnSwipe,
        direction: ActionOnSwipeDirection,
    ) {
        screenModelScope.launch {
            val settings = settingsRepository.currentSettings.value
            val accountId = accountRepository.getActive()?.id ?: return@launch
            val newActions =
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        (settings.actionsOnSwipeToStartComments - action).toSet().toList()
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        (settings.actionsOnSwipeToEndComments - action).toSet().toList()
                    }
                }
            val newSettings =
                when (direction) {
                    ActionOnSwipeDirection.ToStart ->
                        settings.copy(
                            actionsOnSwipeToStartComments = newActions,
                        )

                    ActionOnSwipeDirection.ToEnd ->
                        settings.copy(
                            actionsOnSwipeToEndComments = newActions,
                        )
                }
            settingsRepository.updateSettings(settings = newSettings, accountId = accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            updateState {
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        it.copy(
                            actionsOnSwipeToStartComments = newActions,
                        )
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        it.copy(
                            actionsOnSwipeToEndComments = newActions,
                        )
                    }
                }
            }
        }
    }

    private fun addActionInbox(
        action: ActionOnSwipe,
        direction: ActionOnSwipeDirection,
    ) {
        screenModelScope.launch {
            val settings = settingsRepository.currentSettings.value
            val accountId = accountRepository.getActive()?.id ?: return@launch
            val newActions =
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        (settings.actionsOnSwipeToStartInbox + action).toSet().toList()
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        (settings.actionsOnSwipeToEndInbox + action).toSet().toList()
                    }
                }
            val newSettings =
                when (direction) {
                    ActionOnSwipeDirection.ToStart ->
                        settings.copy(
                            actionsOnSwipeToStartInbox = newActions,
                        )

                    ActionOnSwipeDirection.ToEnd ->
                        settings.copy(
                            actionsOnSwipeToEndInbox = newActions,
                        )
                }
            settingsRepository.updateSettings(settings = newSettings, accountId = accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            updateState {
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        it.copy(
                            actionsOnSwipeToStartInbox = newActions,
                        )
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        it.copy(
                            actionsOnSwipeToEndInbox = newActions,
                        )
                    }
                }
            }
        }
    }

    private fun removeActionInbox(
        action: ActionOnSwipe,
        direction: ActionOnSwipeDirection,
    ) {
        screenModelScope.launch {
            val settings = settingsRepository.currentSettings.value
            val accountId = accountRepository.getActive()?.id ?: return@launch
            val newActions =
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        (settings.actionsOnSwipeToStartInbox - action).toSet().toList()
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        (settings.actionsOnSwipeToEndInbox - action).toSet().toList()
                    }
                }
            val newSettings =
                when (direction) {
                    ActionOnSwipeDirection.ToStart ->
                        settings.copy(
                            actionsOnSwipeToStartInbox = newActions,
                        )

                    ActionOnSwipeDirection.ToEnd ->
                        settings.copy(
                            actionsOnSwipeToEndInbox = newActions,
                        )
                }
            settingsRepository.updateSettings(settings = newSettings, accountId = accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            updateState {
                when (direction) {
                    ActionOnSwipeDirection.ToStart -> {
                        it.copy(
                            actionsOnSwipeToStartInbox = newActions,
                        )
                    }

                    ActionOnSwipeDirection.ToEnd -> {
                        it.copy(
                            actionsOnSwipeToEndInbox = newActions,
                        )
                    }
                }
            }
        }
    }

    private fun resetActionsPosts() {
        val settings = settingsRepository.currentSettings.value
        val newSettings =
            settings.copy(
                actionsOnSwipeToStartPosts = ActionOnSwipe.DEFAULT_SWIPE_TO_START_POSTS,
                actionsOnSwipeToEndPosts = ActionOnSwipe.DEFAULT_SWIPE_TO_START_POSTS,
            )
        screenModelScope.launch {
            val accountId = accountRepository.getActive()?.id ?: return@launch
            settingsRepository.updateSettings(newSettings, accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            refresh()
        }
    }

    private fun resetActionsComments() {
        val settings = settingsRepository.currentSettings.value
        val newSettings =
            settings.copy(
                actionsOnSwipeToStartComments = ActionOnSwipe.DEFAULT_SWIPE_TO_START_COMMENTS,
                actionsOnSwipeToEndComments = ActionOnSwipe.DEFAULT_SWIPE_TO_END_COMMENTS,
            )
        screenModelScope.launch {
            val accountId = accountRepository.getActive()?.id ?: return@launch
            settingsRepository.updateSettings(newSettings, accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            refresh()
        }
    }

    private fun resetActionsInbox() {
        val settings = settingsRepository.currentSettings.value
        val newSettings =
            settings.copy(
                actionsOnSwipeToStartInbox = ActionOnSwipe.DEFAULT_SWIPE_TO_START_INBOX,
                actionsOnSwipeToEndInbox = ActionOnSwipe.DEFAULT_SWIPE_TO_END_INBOX,
            )
        screenModelScope.launch {
            val accountId = accountRepository.getActive()?.id ?: return@launch
            settingsRepository.updateSettings(newSettings, accountId)
            settingsRepository.changeCurrentSettings(newSettings)

            refresh()
        }
    }

    private fun updateAvailableOptions(preventActionsOnBothSides: Boolean = false) {
        val currentState = uiState.value
        val actionsPosts: Set<ActionOnSwipe> =
            buildSet {
                this += ActionOnSwipe.DEFAULT_SWIPE_TO_START_POSTS
                this += ActionOnSwipe.DEFAULT_SWIPE_TO_END_POSTS
                if (preventActionsOnBothSides) {
                    this -= currentState.actionsOnSwipeToStartPosts.toSet()
                    this -= currentState.actionsOnSwipeToEndPosts.toSet()
                }
            }
        val actionsComments: Set<ActionOnSwipe> =
            buildSet {
                this += ActionOnSwipe.DEFAULT_SWIPE_TO_START_COMMENTS
                this += ActionOnSwipe.DEFAULT_SWIPE_TO_END_COMMENTS
                if (preventActionsOnBothSides) {
                    this -= currentState.actionsOnSwipeToStartComments.toSet()
                    this -= currentState.actionsOnSwipeToEndComments.toSet()
                }
            }
        val actionsInbox: Set<ActionOnSwipe> =
            buildSet {
                this += ActionOnSwipe.DEFAULT_SWIPE_TO_START_INBOX
                this += ActionOnSwipe.DEFAULT_SWIPE_TO_END_INBOX
                if (preventActionsOnBothSides) {
                    this -= currentState.actionsOnSwipeToStartInbox.toSet()
                    this -= currentState.actionsOnSwipeToEndInbox.toSet()
                }
            }
        updateState {
            it.copy(
                availableOptionsPosts = actionsPosts.toList(),
                availableOptionsComments = actionsComments.toList(),
                availableOptionsInbox = actionsInbox.toList(),
            )
        }
    }
}
