package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.DefaultInboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions.InboxMentionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies.InboxRepliesViewModel
import org.koin.dsl.module

val inboxTabModule = module {
    single<InboxCoordinator> {
        DefaultInboxCoordinator()
    }
    factory {
        InboxViewModel(
            mvi = DefaultMviModel(InboxMviModel.UiState()),
            identityRepository = get(),
            userRepository = get(),
            coordinator = get(),
        )
    }
    factory {
        InboxRepliesViewModel(
            mvi = DefaultMviModel(InboxRepliesMviModel.UiState()),
            userRepository = get(),
            identityRepository = get(),
            siteRepository = get(),
            hapticFeedback = get(),
            coordinator = get(),
        )
    }
    factory {
        InboxMentionsViewModel(
            mvi = DefaultMviModel(InboxMentionsMviModel.UiState()),
            userRepository = get(),
            identityRepository = get(),
            hapticFeedback = get(),
            coordinator = get(),
        )
    }
}
