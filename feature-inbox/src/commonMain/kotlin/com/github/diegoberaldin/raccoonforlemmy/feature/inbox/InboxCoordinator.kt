package com.github.diegoberaldin.raccoonforlemmy.feature.inbox

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface InboxCoordinator {
    val unreadOnly: StateFlow<Boolean>
    val effects: SharedFlow<InboxMviModel.Effect>

    fun setUnreadOnly(value: Boolean)

    suspend fun emitEffect(effect: InboxMviModel.Effect)
}
