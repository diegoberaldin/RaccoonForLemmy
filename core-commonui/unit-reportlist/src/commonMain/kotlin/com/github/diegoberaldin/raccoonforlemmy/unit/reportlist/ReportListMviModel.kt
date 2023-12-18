package com.github.diegoberaldin.raccoonforlemmy.unit.reportlist

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentReportModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostReportModel

enum class ReportListSection {
    Posts,
    Comments,
}

interface ReportListMviModel :
    MviModel<ReportListMviModel.Intent, ReportListMviModel.UiState, ReportListMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object HapticIndication : Intent
        data class ChangeSection(val value: ReportListSection) : Intent
        data class ChangeUnresolvedOnly(val value: Boolean) : Intent
        data object LoadNextPage : Intent
        data object Refresh : Intent
        data class ResolvePost(val id: Int) : Intent
        data class ResolveComment(val id: Int) : Intent
    }

    data class UiState(
        val section: ReportListSection = ReportListSection.Posts,
        val unresolvedOnly: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val initial: Boolean = true,
        val asyncInProgress: Boolean = false,
        val swipeActionsEnabled: Boolean = true,
        val autoLoadImages: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
        val canFetchMore: Boolean = true,
        val postReports: List<PostReportModel> = emptyList(),
        val commentReports: List<CommentReportModel> = emptyList(),
    )

    sealed interface Effect {
        data object BackToTop : Effect
    }
}
