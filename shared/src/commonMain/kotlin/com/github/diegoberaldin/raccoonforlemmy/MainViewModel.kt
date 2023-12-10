package com.github.diegoberaldin.raccoonforlemmy

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.InboxCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    private val mvi: DefaultMviModel<MainScreenMviModel.Intent, MainScreenMviModel.UiState, MainScreenMviModel.Effect>,
    private val inboxCoordinator: InboxCoordinator,
) : MainScreenMviModel,
    MviModel<MainScreenMviModel.Intent, MainScreenMviModel.UiState, MainScreenMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch(Dispatchers.IO) {
            inboxCoordinator.totalUnread.onEach { unreadCount ->
                mvi.emitEffect(MainScreenMviModel.Effect.UnreadItemsDetected(unreadCount))
            }.launchIn(this)
        }
    }

    override fun reduce(intent: MainScreenMviModel.Intent) {
        when (intent) {
            is MainScreenMviModel.Intent.SetBottomBarOffsetHeightPx -> {
                mvi.updateState { it.copy(bottomBarOffsetHeightPx = intent.value) }
            }
        }
    }
}