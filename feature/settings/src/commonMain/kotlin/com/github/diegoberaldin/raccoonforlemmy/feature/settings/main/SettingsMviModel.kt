package com.github.diegoberaldin.raccoonforlemmy.feature.settings.main

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.UrlOpeningMode
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface SettingsMviModel :
    MviModel<SettingsMviModel.Intent, SettingsMviModel.UiState, SettingsMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class ChangeUiTheme(val value: UiTheme?) : Intent
        data class ChangeLanguage(val value: String) : Intent
        data class ChangeIncludeNsfw(val value: Boolean) : Intent
        data class ChangeBlurNsfw(val value: Boolean) : Intent
        data class ChangeEnableSwipeActions(val value: Boolean) : Intent
        data class ChangeCrashReportEnabled(val value: Boolean) : Intent
    }

    data class UiState(
        val isLogged: Boolean = false,
        val uiTheme: UiTheme? = null,
        val lang: String = "",
        val defaultListingType: ListingType = ListingType.Local,
        val defaultPostSortType: SortType = SortType.Active,
        val defaultCommentSortType: SortType = SortType.New,
        val enableSwipeActions: Boolean = true,
        val includeNsfw: Boolean = true,
        val blurNsfw: Boolean = true,
        val urlOpeningMode: UrlOpeningMode = UrlOpeningMode.Internal,
        val crashReportEnabled: Boolean = false,
        val availableSortTypesForPosts: List<SortType> = emptyList(),
        val availableSortTypesForComments: List<SortType> = emptyList(),
        val customTabsEnabled: Boolean = true,
    )

    sealed interface Effect
}
