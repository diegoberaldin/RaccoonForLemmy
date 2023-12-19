package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesMviModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun getInboxViewModel(): InboxMviModel = InboxScreenModelHelper.model

actual fun getInboxRepliesViewModel(): InboxRepliesMviModel = InboxScreenModelHelper.repliesModel

actual fun getInboxMentionsViewModel(): InboxMentionsMviModel = InboxScreenModelHelper.mentionsModel

actual fun getInboxMessagesViewModel(): InboxMessagesMviModel = InboxScreenModelHelper.messagesModel

object InboxScreenModelHelper : KoinComponent {
    val model: InboxMviModel by inject()
    val repliesModel: InboxRepliesMviModel by inject()
    val mentionsModel: InboxMentionsMviModel by inject()
    val messagesModel: InboxMessagesMviModel by inject()
}
