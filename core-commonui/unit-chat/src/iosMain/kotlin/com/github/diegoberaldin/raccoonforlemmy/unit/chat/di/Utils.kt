package com.github.diegoberaldin.raccoonforlemmy.unit.chat.di

import com.github.diegoberaldin.raccoonforlemmy.unit.chat.InboxChatMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getInboxChatViewModel(otherUserId: Int): InboxChatMviModel =
    UnitChatDiHelper.getChatViewModel(otherUserId)

object UnitChatDiHelper : KoinComponent {
    fun getChatViewModel(otherUserId: Int): InboxChatMviModel {
        val model: InboxChatMviModel by inject(
            parameters = { parametersOf(otherUserId) }
        )
        return model
    }
}