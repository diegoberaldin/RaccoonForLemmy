package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesMviModel

expect fun getInboxViewModel(): InboxMviModel

expect fun getInboxRepliesViewModel(): InboxRepliesMviModel

expect fun getInboxMentionsViewModel(): InboxMentionsMviModel

expect fun getInboxMessagesViewModel(): InboxMessagesMviModel
