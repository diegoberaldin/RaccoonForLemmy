package com.github.diegoberaldin.raccoonforlemmy.unit.drawer

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Stable
interface ModalDrawerMviModel :
    MviModel<ModalDrawerMviModel.Intent, ModalDrawerMviModel.UiState, ModalDrawerMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object Refresh : Intent

        data class SetSearch(
            val value: String,
        ) : Intent

        data class ToggleFavorite(
            val id: Long,
        ) : Intent
    }

    data class UiState(
        val loading: Boolean = false,
        val user: UserModel? = null,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val refreshing: Boolean = false,
        val instance: String? = null,
        val communities: List<CommunityModel> = emptyList(),
        val multiCommunities: List<MultiCommunityModel> = emptyList(),
        val favorites: List<CommunityModel> = emptyList(),
        val searchText: String = "",
        val isFiltering: Boolean = false,
        val enableToggleFavorite: Boolean = false,
    )

    sealed interface Effect
}
