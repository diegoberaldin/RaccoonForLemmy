package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import dev.icerock.moko.resources.desc.StringDesc

interface MultiCommunityEditorMviModel :
    MviModel<MultiCommunityEditorMviModel.Intent, MultiCommunityEditorMviModel.UiState, MultiCommunityEditorMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data class SetName(val value: String) : Intent
        data class SetSearch(val value: String) : Intent
        data class SelectImage(val index: Int?) : Intent
        data class ToggleCommunity(val id: Int) : Intent
        data object Submit : Intent
    }

    data class UiState(
        val name: String = "",
        val autoLoadImages: Boolean = true,
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