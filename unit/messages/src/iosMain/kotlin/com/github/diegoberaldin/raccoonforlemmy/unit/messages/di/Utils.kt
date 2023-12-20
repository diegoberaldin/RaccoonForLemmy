package com.github.diegoberaldin.raccoonforlemmy.unit.messages.di

import com.github.diegoberaldin.raccoonforlemmy.unit.messages.InboxMessagesMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getInboxMessagesViewModel(): InboxMessagesMviModel = UnitMessagesDiHelper.messagesModel

object UnitMessagesDiHelper : KoinComponent {
    val messagesModel: InboxMessagesMviModel by inject()
}
