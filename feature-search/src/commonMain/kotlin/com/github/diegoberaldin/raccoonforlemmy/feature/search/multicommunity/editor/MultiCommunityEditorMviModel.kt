package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.editor

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import dev.icerock.moko.resources.desc.StringDesc

interface MultiCommunityEditorMviModel :
    MviModel<MultiCommunityEditorMviModel.Intent, MultiCommunityEditorMviModel.UiState, MultiCommunityEditorMviModel.Effect> {
    sealed interface Intent {
        data class SetName(val value: String) : Intent
        data class SetSearch(val value: String) : Intent
        data class SelectImage(val index: Int?) : Intent
        data class ToggleCommunity(val index: Int) : Intent
        data object Submit : Intent
    }

    data class UiState(
        val name: String = "",
        val nameError: StringDesc? = null,
        val icon: String? = null,
        val availableIcons: List<String> = emptyList(),
        val communities: List<Pair<CommunityModel, Boolean>> = emptyList(),
        val searchText: String = "",
    )

    sealed interface Effect {
        data object Close : Effect
    }
}