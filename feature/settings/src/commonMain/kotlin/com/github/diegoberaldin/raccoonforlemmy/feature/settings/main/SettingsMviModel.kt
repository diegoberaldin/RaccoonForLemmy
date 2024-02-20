package com.github.diegoberaldin.raccoonforlemmy.feature.settings.main

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface SettingsMviModel :
    MviModel<SettingsMviModel.Intent, SettingsMviModel.UiState, SettingsMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class ChangeUiTheme(val value: UiTheme?) : Intent
        data class ChangeLanguage(val value: String) : Intent
        data class ChangeNavBarTitlesVisible(val value: Boolean) : Intent
        data class ChangeIncludeNsfw(val value: Boolean) : Intent
        data class ChangeBlurNsfw(val value: Boolean) : Intent
        data class ChangeOpenUrlsInExternalBrowser(val value: Boolean) : Intent
        data class ChangeEnableSwipeActions(val value: Boolean) : Intent
        data class ChangeEnableDoubleTapAction(val value: Boolean) : Intent
        data class ChangeCrashReportEnabled(val value: Boolean) : Intent
        data class ChangeAutoLoadImages(val value: Boolean) : Intent
        data class ChangeAutoExpandComments(val value: Boolean) : Intent
        data class ChangeHideNavigationBarWhileScrolling(val value: Boolean) : Intent
        data class ChangeMarkAsReadWhileScrolling(val value: Boolean) : Intent
        data class ChangeDefaultInboxUnreadOnly(val value: Boolean) : Intent
        data class ChangeSearchPostTitleOnly(val value: Boolean) : Intent
        data class ChangeEdgeToEdge(val value: Boolean) : Intent
        data class ChangeInfiniteScrollDisabled(val value: Boolean) : Intent
    }

    data class UiState(
        val isLogged: Boolean = false,
        val uiTheme: UiTheme? = null,
        val lang: String = "",
        val defaultListingType: ListingType = ListingType.Local,
        val defaultPostSortType: SortType = SortType.Active,
        val defaultCommentSortType: SortType = SortType.New,
        val defaultInboxUnreadOnly: Boolean = true,
        val navBarTitlesVisible: Boolean = false,
        val includeNsfw: Boolean = true,
        val blurNsfw: Boolean = true,
        val openUrlsInExternalBrowser: Boolean = false,
        val enableSwipeActions: Boolean = true,
        val enableDoubleTapAction: Boolean = true,
        val crashReportEnabled: Boolean = false,
        val autoLoadImages: Boolean = false,
        val autoExpandComments: Boolean = false,
        val hideNavigationBarWhileScrolling: Boolean = true,
        val zombieModeInterval: Duration = 2.5.seconds,
        val zombieModeScrollAmount: Float = 100f,
        val markAsReadWhileScrolling: Boolean = true,
        val availableSortTypesForPosts: List<SortType> = emptyList(),
        val availableSortTypesForComments: List<SortType> = emptyList(),
        val searchPostTitleOnly: Boolean = false,
        val edgeToEdge: Boolean = true,
        val infiniteScrollDisabled: Boolean = false,
        val opaqueSystemBars: Boolean = false,
    )

    sealed interface Effect
}
