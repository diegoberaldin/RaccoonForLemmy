package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.detail.InboxChatViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.list.InboxMessagesViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

actual fun getInboxViewModel() = InboxScreenModelHelper.model

actual fun getInboxRepliesViewModel() = InboxScreenModelHelper.repliesModel

actual fun getInboxMentionsViewModel() = InboxScreenModelHelper.mentionsModel

actual fun getInboxMessagesViewModel() = InboxScreenModelHelper.messagesModel

actual fun getInboxChatViewModel(otherUserId: Int) =
    InboxScreenModelHelper.getChatViewModel(otherUserId)

object InboxScreenModelHelper : KoinComponent {
    val model: InboxViewModel by inject()
    val repliesModel: InboxRepliesViewModel by inject()
    val mentionsModel: InboxMentionsViewModel by inject()
    val messagesModel: InboxMessagesViewModel by inject()

    fun getChatViewModel(otherUserId: Int): InboxChatViewModel {
        val model: InboxChatViewModel by inject(
            parameters = { parametersOf(otherUserId) }
        )
        return model
    }
}
