package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.detail.InboxChatViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.list.InboxMessagesViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesViewModel
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

actual fun getInboxViewModel(): InboxViewModel {
    val res: InboxViewModel by inject(InboxViewModel::class.java)
    return res
}

actual fun getInboxRepliesViewModel(): InboxRepliesViewModel {
    val res: InboxRepliesViewModel by inject(InboxRepliesViewModel::class.java)
    return res
}

actual fun getInboxMentionsViewModel(): InboxMentionsViewModel {
    val res: InboxMentionsViewModel by inject(InboxMentionsViewModel::class.java)
    return res
}

actual fun getInboxMessagesViewModel(): InboxMessagesViewModel {
    val res: InboxMessagesViewModel by inject(InboxMessagesViewModel::class.java)
    return res
}

actual fun getInboxChatViewModel(otherUserId: Int): InboxChatViewModel {
    val res: InboxChatViewModel by inject(
        InboxChatViewModel::class.java,
        parameters = {
            parametersOf(otherUserId)
        },
    )
    return res
}
