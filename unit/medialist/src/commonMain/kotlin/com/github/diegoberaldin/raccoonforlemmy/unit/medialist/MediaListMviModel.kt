package com.github.diegoberaldin.raccoonforlemmy.unit.medialist

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MediaModel

interface MediaListMviModel :
    ScreenModel,
    MviModel<MediaListMviModel.Intent, MediaListMviModel.State, MediaListMviModel.Effect> {
    sealed interface Intent {
        data object Refresh : Intent

        data object LoadNextPage : Intent

        data class Delete(val media: MediaModel) : Intent
    }

    data class State(
        val initial: Boolean = true,
        val canFetchMore: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val media: List<MediaModel> = emptyList(),
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val fullWidthImages: Boolean = false,
    )

    sealed interface Effect {
        data class Failure(val message: String?) : Effect
    }
}
