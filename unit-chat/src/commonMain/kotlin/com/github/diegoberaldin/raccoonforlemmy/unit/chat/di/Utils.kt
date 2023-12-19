package com.github.diegoberaldin.raccoonforlemmy.unit.chat.di

import com.github.diegoberaldin.raccoonforlemmy.unit.chat.InboxChatMviModel

expect fun getInboxChatViewModel(otherUserId: Int): InboxChatMviModel
