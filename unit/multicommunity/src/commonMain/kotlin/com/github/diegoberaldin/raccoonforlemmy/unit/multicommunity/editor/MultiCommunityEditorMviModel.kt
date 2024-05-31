package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ValidationError
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel

interface MultiCommunityEditorMviModel :
    MviModel<MultiCommunityEditorMviModel.Intent, MultiCommunityEditorMviModel.UiState, MultiCommunityEditorMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class SetName(val value: String) : Intent

        data class SetSearch(val value: String) : Intent

        data class SelectImage(val index: Int?) : Intent

        data class ToggleCommunity(val id: Long) : Intent

        data object LoadNextPage : Intent

        data object Submit : Intent
    }

    data class UiState(
        val name: String = "",
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val nameError: ValidationError? = null,
        val icon: String? = null,
        val availableIcons: List<String> = emptyList(),
        val communities: List<CommunityModel> = emptyList(),
        val selectedCommunityIds: List<Long> = emptyList(),
        val searchText: String = "",
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
    )

    sealed interface Effect {
        data object Close : Effect
    }
}
