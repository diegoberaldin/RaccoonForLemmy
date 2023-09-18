package com.github.diegoberaldin.raccoonforlemmy.feature.search.content

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface ExploreMviModel :
    MviModel<ExploreMviModel.Intent, ExploreMviModel.UiState, ExploreMviModel.Effect> {
    sealed interface Intent {
        object Refresh : Intent
        object LoadNextPage : Intent
        data class SetSearch(val value: String) : Intent
        data class SetListingType(val value: ListingType) : Intent
        data class SetSortType(val value: SortType) : Intent
        data class SetResultType(val value: SearchResultType) : Intent
        data class UpVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val index: Int, val feedback: Boolean = false) : Intent
        data class UpVoteComment(val index: Int, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val index: Int, val feedback: Boolean = false) : Intent
        data class SaveComment(val index: Int, val feedback: Boolean = false) : Intent
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
    )

    sealed interface Effect
}
