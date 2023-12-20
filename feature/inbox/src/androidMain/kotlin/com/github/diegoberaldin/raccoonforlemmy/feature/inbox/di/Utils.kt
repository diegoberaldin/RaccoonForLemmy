package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesMviModel
import org.koin.java.KoinJavaComponent.inject

actual fun getInboxViewModel(): InboxMviModel {
    val res: InboxMviModel by inject(InboxMviModel::class.java)
    return res
}

actual fun getInboxMentionsViewModel(): InboxMentionsMviModel {
    val res: InboxMentionsMviModel by inject(InboxMentionsMviModel::class.java)
    return res
}

actual fun getInboxMessagesViewModel(): InboxMessagesMviModel {
    val res: InboxMessagesMviModel by inject(InboxMessagesMviModel::class.java)
    return res
}
