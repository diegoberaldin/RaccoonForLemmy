package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.DefaultInboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.InboxMessagesViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.usecase.DefaultGetUnreadItemsUseCase
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.usecase.GetUnreadItemsUseCase
import org.koin.dsl.module

val inboxTabModule = module {
    single<InboxCoordinator> {
        DefaultInboxCoordinator(
            identityRepository = get(),
            getUnreadItemsUseCase = get(),
        )
    }
    single<GetUnreadItemsUseCase> {
        DefaultGetUnreadItemsUseCase(
            identityRepository = get(),
            userRepository = get(),
            messageRepository = get(),
        )
    }
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
    factory<InboxRepliesMviModel> {
        InboxRepliesViewModel(
            mvi = DefaultMviModel(InboxRepliesMviModel.UiState()),
            userRepository = get(),
            identityRepository = get(),
            siteRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            hapticFeedback = get(),
            coordinator = get(),
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
