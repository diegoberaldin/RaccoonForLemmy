package com.github.diegoberaldin.raccoonforlemmy.feature.search.main

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface ExploreMviModel :
    MviModel<ExploreMviModel.Intent, ExploreMviModel.UiState, ExploreMviModel.Effect>, ScreenModel {
    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class SetSearch(val value: String) : Intent
        data class SetListingType(val value: ListingType) : Intent
        data class SetSortType(val value: SortType) : Intent
        data class SetResultType(val value: SearchResultType) : Intent
        data class UpVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Int, val feedback: Boolean = false) : Intent
        data class UpVoteComment(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val id: Int, val feedback: Boolean = false) : Intent
        data class SaveComment(val id: Int, val feedback: Boolean = false) : Intent
    }

    data class UiState(
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val isLogged: Boolean = false,
        val blurNsfw: Boolean = true,
        val instance: String = "",
        val searchText: String = "",
        val listingType: ListingType = ListingType.Local,
        val sortType: SortType = SortType.Active,
        val results: List<Any> = emptyList(),
        val resultType: SearchResultType = SearchResultType.All,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val separateUpAndDownVotes: Boolean = false,
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect {
        data object BackToTop : Effect
    }
}
