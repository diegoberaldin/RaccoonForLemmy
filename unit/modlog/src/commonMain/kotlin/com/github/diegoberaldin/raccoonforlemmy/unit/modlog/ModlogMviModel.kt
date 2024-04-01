package com.github.diegoberaldin.raccoonforlemmy.unit.modlog

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem

interface ModlogMviModel :
    MviModel<ModlogMviModel.Intent, ModlogMviModel.UiState, ModlogMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
    }

    data class UiState(
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val items: List<ModlogItem> = emptyList(),
    )

    sealed interface Effect {
        data object BackToTop : Effect
    }
}
