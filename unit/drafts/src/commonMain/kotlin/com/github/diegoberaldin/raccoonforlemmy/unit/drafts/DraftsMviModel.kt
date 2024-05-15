package com.github.diegoberaldin.raccoonforlemmy.unit.drafts

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftModel

sealed interface DraftsSection {
    data object Posts : DraftsSection

    data object Comments : DraftsSection
}

interface DraftsMviModel :
    ScreenModel,
    MviModel<DraftsMviModel.Intent, DraftsMviModel.State, DraftsMviModel.Effect> {
    sealed interface Intent {
        data object Refresh : Intent
        data class ChangeSection(val section: DraftsSection) : Intent
        data class Delete(val model: DraftModel) : Intent
    }

    data class State(
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val postLayout: PostLayout = PostLayout.Card,
        val section: DraftsSection = DraftsSection.Posts,
        val postDrafts: List<DraftModel> = emptyList(),
        val commentDrafts: List<DraftModel> = emptyList(),
    )

    sealed interface Effect
}
