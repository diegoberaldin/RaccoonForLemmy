package com.github.diegoberaldin.raccoonforlemmy.unit.mentions.di

import com.github.diegoberaldin.raccoonforlemmy.unit.mentions.InboxMentionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.mentions.InboxMentionsViewModel
import org.koin.dsl.module

val inboxMentionsModule = module {
    factory<InboxMentionsMviModel> {
        InboxMentionsViewModel(
            userRepository = get(),
            identityRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            hapticFeedback = get(),
            coordinator = get(),
            notificationCenter = get(),
        )
    }
}