package com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.comments

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel

interface ModdedCommentsMviModel :
    ScreenModel,
    MviModel<ModdedCommentsMviModel.Intent, ModdedCommentsMviModel.State, ModdedCommentsMviModel.Effect> {
    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class UpVoteComment(val commentId: Int, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val commentId: Int, val feedback: Boolean = false) : Intent
        data class SaveComment(val commentId: Int, val feedback: Boolean = false) : Intent
        data object HapticIndication : Intent
        data class ModDistinguishComment(val commentId: Int) : Intent
    }

    data class State(
        val initial: Boolean = true,
        val loading: Boolean = false,
        val refreshing: Boolean = true,
        val canFetchMore: Boolean = true,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val swipeActionsEnabled: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val comments: List<CommentModel> = emptyList(),
        val actionsOnSwipeToStartComments: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndComments: List<ActionOnSwipe> = emptyList(),
    )

    sealed interface Effect
}