package com.github.diegoberaldin.raccoonforlemmy.unit.chat.di

import com.github.diegoberaldin.raccoonforlemmy.unit.chat.InboxChatMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.chat.InboxChatViewModel
import org.koin.dsl.module

val chatModule = module {
    factory<InboxChatMviModel> { params ->
        InboxChatViewModel(
            otherUserId = params[0],
            identityRepository = get(),
            siteRepository = get(),
            userRepository = get(),
            messageRepository = get(),
            postRepository = get(),
            notificationCenter = get(),
            settingsRepository = get(),
        )
    }
}