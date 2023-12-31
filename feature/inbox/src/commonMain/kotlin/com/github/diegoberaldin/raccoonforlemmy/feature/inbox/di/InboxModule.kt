package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.mentions.di.inboxMentionsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.messages.di.inboxMessagesModule
import com.github.diegoberaldin.raccoonforlemmy.unit.replies.di.inboxRepliesModule
import org.koin.dsl.module

val inboxTabModule = module {
    includes(
        inboxRepliesModule,
        inboxMessagesModule,
        inboxMentionsModule,
    )
    factory<InboxMviModel> {
        InboxViewModel(
            mvi = DefaultMviModel(InboxMviModel.UiState()),
            identityRepository = get(),
            userRepository = get(),
            coordinator = get(),
            settingsRepository = get(),
            notificationCenter = get(),
            contentResetCoordinator = get(),
        )
    }
}
