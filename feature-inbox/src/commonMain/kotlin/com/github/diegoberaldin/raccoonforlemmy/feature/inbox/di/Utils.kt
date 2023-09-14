package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.detail.InboxChatViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.list.InboxMessagesViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesViewModel

expect fun getInboxViewModel(): InboxViewModel

expect fun getInboxRepliesViewModel(): InboxRepliesViewModel

expect fun getInboxMentionsViewModel(): InboxMentionsViewModel

expect fun getInboxMessagesViewModel(): InboxMessagesViewModel

expect fun getInboxChatViewModel(otherUserId: Int): InboxChatViewModel
