package com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

@Stable
interface InstanceInfoMviModel :
    MviModel<InstanceInfoMviModel.Intent, InstanceInfoMviModel.UiState, InstanceInfoMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class ChangeSortType(val value: SortType) : Intent
    }

    data class UiState(
        val title: String = "",
        val description: String = "",
        val canFetchMore: Boolean = true,
        val refreshing: Boolean = false,
        val autoLoadImages: Boolean = true,
        val loading: Boolean = false,
        val sortType: SortType = SortType.Active,
        val communities: List<CommunityModel> = emptyList(),
        val availableSortTypes: List<SortType> = emptyList(),
    )

    sealed interface Effect {
        data object BackToTop : Effect
    }
}
