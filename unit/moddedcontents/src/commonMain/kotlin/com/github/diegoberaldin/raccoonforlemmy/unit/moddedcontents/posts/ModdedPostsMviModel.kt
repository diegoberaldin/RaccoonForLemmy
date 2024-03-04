package com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.posts

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel

interface ModdedPostsMviModel :
    ScreenModel,
    MviModel<ModdedPostsMviModel.Intent, ModdedPostsMviModel.State, ModdedPostsMviModel.Effect> {
    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class UpVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Int, val feedback: Boolean = false) : Intent
        data object HapticIndication : Intent
        data class ModFeaturePost(val id: Int) : Intent
        data class ModLockPost(val id: Int) : Intent
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
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val posts: List<PostModel> = emptyList(),
        val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = emptyList(),
    )

    sealed interface Effect
}