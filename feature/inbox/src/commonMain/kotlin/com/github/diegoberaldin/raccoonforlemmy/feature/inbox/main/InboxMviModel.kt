package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel

interface InboxMviModel :
    MviModel<InboxMviModel.Intent, InboxMviModel.UiState, InboxMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class ChangeSection(
            val value: InboxSection,
        ) : Intent

        data object ReadAll : Intent
    }

    data class UiState(
        val isLogged: Boolean? = null,
        val section: InboxSection = InboxSection.Replies,
        val unreadOnly: Boolean = true,
        val unreadReplies: Int = 0,
        val unreadMentions: Int = 0,
        val unreadMessages: Int = 0,
    )

    sealed interface Effect {
        data object Refresh : Effect

        data object ReadAllInboxSuccess : Effect
    }
}
