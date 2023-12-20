package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesViewModel
import org.koin.dsl.module

val inboxTabModule = module {
    factory<InboxMviModel> {
        InboxViewModel(
            mvi = DefaultMviModel(InboxMviModel.UiState()),
            identityRepository = get(),
            userRepository = get(),
            coordinator = get(),
            settingsRepository = get(),
            notificationCenter = get(),
        )
    }
    factory<InboxMentionsMviModel> {
        InboxMentionsViewModel(
            mvi = DefaultMviModel(InboxMentionsMviModel.UiState()),
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
    factory<InboxMessagesMviModel> {
        InboxMessagesViewModel(
            mvi = DefaultMviModel(InboxMessagesMviModel.UiState()),
            identityRepository = get(),
            siteRepository = get(),
            messageRepository = get(),
            coordinator = get(),
            notificationCenter = get(),
            settingsRepository = get(),
        )
    }
}
