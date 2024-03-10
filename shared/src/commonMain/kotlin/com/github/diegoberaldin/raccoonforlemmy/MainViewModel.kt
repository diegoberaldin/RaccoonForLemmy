package com.github.diegoberaldin.raccoonforlemmy

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    private val inboxCoordinator: InboxCoordinator,
    private val identityRepository: IdentityRepository,
) : MainScreenMviModel,
    DefaultMviModel<MainScreenMviModel.Intent, MainScreenMviModel.UiState, MainScreenMviModel.Effect>(
        initialState = MainScreenMviModel.UiState(),
    ) {

    init {
        screenModelScope.launch(Dispatchers.IO) {
            identityRepository.startup()

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