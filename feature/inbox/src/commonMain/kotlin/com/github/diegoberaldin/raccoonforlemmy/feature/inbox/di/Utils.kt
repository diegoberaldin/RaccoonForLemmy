package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesMviModel

expect fun getInboxViewModel(): InboxMviModel

expect fun getInboxMessagesViewModel(): InboxMessagesMviModel
