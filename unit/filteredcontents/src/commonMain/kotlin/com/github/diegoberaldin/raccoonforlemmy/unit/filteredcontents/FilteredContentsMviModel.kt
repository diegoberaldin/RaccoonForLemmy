package com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel

sealed interface FilteredContentsType {
    data object Votes : FilteredContentsType
    data object Moderated : FilteredContentsType
}

sealed interface FilteredContentsSection {
    data object Posts : FilteredContentsSection

    data object Comments : FilteredContentsSection
}

interface FilteredContentsMviModel :
    ScreenModel,
    MviModel<FilteredContentsMviModel.Intent, FilteredContentsMviModel.State, FilteredContentsMviModel.Effect> {
    sealed interface Intent {
        data class ChangeSection(val value: FilteredContentsSection) : Intent
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class UpVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Int, val feedback: Boolean = false) : Intent

        data class UpVoteComment(val commentId: Int, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val commentId: Int, val feedback: Boolean = false) : Intent
        data class SaveComment(val commentId: Int, val feedback: Boolean = false) : Intent
        data object HapticIndication : Intent
        data class ModFeaturePost(val id: Int) : Intent
        data class ModLockPost(val id: Int) : Intent

        data class ModDistinguishComment(val commentId: Int) : Intent
    }

    data class State(
        val contentsType: FilteredContentsType = FilteredContentsType.Votes,
        val liked: Boolean = true,
        val initial: Boolean = true,
        val loading: Boolean = false,
        val refreshing: Boolean = true,
        val canFetchMore: Boolean = true,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val swipeActionsEnabled: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val section: FilteredContentsSection = FilteredContentsSection.Posts,
        val posts: List<PostModel> = emptyList(),
        val comments: List<CommentModel> = emptyList(),
        val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToStartComments: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndComments: List<ActionOnSwipe> = emptyList(),
    )

    sealed interface Effect {
        data object BackToTop : Effect
    }
}