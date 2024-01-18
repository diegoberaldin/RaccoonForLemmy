package com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository

class ConfigureSwipeActionsViewModel(
    private val mvi: DefaultMviModel<ConfigureSwipeActionsMviModel.Intent, ConfigureSwipeActionsMviModel.UiState, ConfigureSwipeActionsMviModel.Effect>,
    private val settingsRepository: SettingsRepository,
) : ConfigureSwipeActionsMviModel,
    MviModel<ConfigureSwipeActionsMviModel.Intent, ConfigureSwipeActionsMviModel.UiState, ConfigureSwipeActionsMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        val settings = settingsRepository.currentSettings.value
        mvi.updateState {
            it.copy(
                actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts,
                actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts,
                actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                actionsOnSwipeToStartInbox = settings.actionsOnSwipeToStartInbox,
                actionsOnSwipeToEndInbox = settings.actionsOnSwipeToEndInbox,
            )
        }
    }

    override fun reduce(intent: ConfigureSwipeActionsMviModel.Intent) {
        when (intent) {
            ConfigureSwipeActionsMviModel.Intent.AddActionComments -> TODO()
            ConfigureSwipeActionsMviModel.Intent.AddActionInbox -> TODO()
            ConfigureSwipeActionsMviModel.Intent.AddActionPosts -> TODO()
            is ConfigureSwipeActionsMviModel.Intent.DeleteActionComments -> TODO()
            is ConfigureSwipeActionsMviModel.Intent.DeleteActionInbox -> TODO()
            is ConfigureSwipeActionsMviModel.Intent.DeleteActionPost -> TODO()
            ConfigureSwipeActionsMviModel.Intent.ResetActionsComments -> TODO()
            ConfigureSwipeActionsMviModel.Intent.ResetActionsInbox -> TODO()
            ConfigureSwipeActionsMviModel.Intent.ResetActionsPosts -> TODO()
        }
    }
}