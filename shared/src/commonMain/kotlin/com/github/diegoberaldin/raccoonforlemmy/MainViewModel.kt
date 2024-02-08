package com.github.diegoberaldin.raccoonforlemmy

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    private val inboxCoordinator: InboxCoordinator,
) : MainScreenMviModel,
    DefaultMviModel<MainScreenMviModel.Intent, MainScreenMviModel.UiState, MainScreenMviModel.Effect>(
        initialState = MainScreenMviModel.UiState(),
    ) {

    override fun onStarted() {
        super.onStarted()
        scope?.launch(Dispatchers.IO) {
            inboxCoordinator.totalUnread.onEach { unreadCount ->
                emitEffect(MainScreenMviModel.Effect.UnreadItemsDetected(unreadCount))
            }.launchIn(this)
        }
    }

    override fun reduce(intent: MainScreenMviModel.Intent) {
        when (intent) {
            is MainScreenMviModel.Intent.SetBottomBarOffsetHeightPx -> {
                updateState { it.copy(bottomBarOffsetHeightPx = intent.value) }
            }
        }
    }
}