package com.github.diegoberaldin.raccoonforlemmy.feature.inbox

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultInboxCoordinator : InboxCoordinator {

    override val unreadOnly = MutableStateFlow(true)
    override val effects = MutableSharedFlow<InboxMviModel.Effect>()

    override fun setUnreadOnly(value: Boolean) {
        unreadOnly.value = value
    }

    override suspend fun emitEffect(effect: InboxMviModel.Effect) {
        effects.emit(effect)
    }
}