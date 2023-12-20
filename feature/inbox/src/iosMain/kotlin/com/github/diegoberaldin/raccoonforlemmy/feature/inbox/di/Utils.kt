package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getInboxViewModel(): InboxMviModel = InboxScreenModelHelper.model

actual fun getInboxMessagesViewModel(): InboxMessagesMviModel = InboxScreenModelHelper.messagesModel

object InboxScreenModelHelper : KoinComponent {
    val model: InboxMviModel by inject()
    val messagesModel: InboxMessagesMviModel by inject()
}
