package com.github.diegoberaldin.raccoonforlemmy.unit.mentions.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.mentions.InboxMentionsMviModel
import com.github.diegoberaldin.raccoonforlemmy.unit.mentions.InboxMentionsViewModel
import org.koin.dsl.module

val mentionsModule = module {
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
}