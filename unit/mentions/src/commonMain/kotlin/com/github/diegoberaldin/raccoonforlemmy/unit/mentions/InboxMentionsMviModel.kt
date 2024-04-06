package com.github.diegoberaldin.raccoonforlemmy.unit.mentions

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel

@Stable
interface InboxMentionsMviModel :
    MviModel<InboxMentionsMviModel.Intent, InboxMentionsMviModel.UiState, InboxMentionsMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class MarkAsRead(val read: Boolean, val id: Long) : Intent
        data object HapticIndication : Intent
        data class UpVoteComment(val id: Long) : Intent
        data class DownVoteComment(val id: Long) : Intent
    }

    data class UiState(
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val unreadOnly: Boolean = true,
        val mentions: List<PersonMentionModel> = emptyList(),
        val swipeActionsEnabled: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val showScores: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val actionsOnSwipeToStartInbox: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndInbox: List<ActionOnSwipe> = emptyList(),
    )

    sealed interface Effect {
        data class UpdateUnreadItems(val value: Int) : Effect
        data object BackToTop : Effect
    }
}
