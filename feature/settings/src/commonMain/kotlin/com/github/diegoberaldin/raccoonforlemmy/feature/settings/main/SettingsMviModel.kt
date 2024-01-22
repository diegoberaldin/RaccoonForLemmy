package com.github.diegoberaldin.raccoonforlemmy.feature.settings.main

import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.CommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.FontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
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
        data class ChangeUiFontSize(val value: Float) : Intent
        data class ChangeUiFontFamily(val value: UiFontFamily) : Intent
        data class ChangeContentFontSize(val value: Float) : Intent
        data class ChangeContentFontFamily(val value: UiFontFamily) : Intent
        data class ChangeLanguage(val value: String) : Intent
        data class ChangeDefaultListingType(val value: ListingType) : Intent
        data class ChangePostLayout(val value: PostLayout) : Intent
        data class ChangeDefaultPostSortType(val value: SortType) : Intent
        data class ChangeDefaultCommentSortType(val value: SortType) : Intent
        data class ChangeNavBarTitlesVisible(val value: Boolean) : Intent
        data class ChangeDynamicColors(val value: Boolean) : Intent
        data class ChangeIncludeNsfw(val value: Boolean) : Intent
        data class ChangeBlurNsfw(val value: Boolean) : Intent
        data class ChangeOpenUrlsInExternalBrowser(val value: Boolean) : Intent
        data class ChangeEnableSwipeActions(val value: Boolean) : Intent
        data class ChangeEnableDoubleTapAction(val value: Boolean) : Intent
        data class ChangeCustomSeedColor(val value: Color?) : Intent
        data class ChangeUpvoteColor(val value: Color?) : Intent
        data class ChangeDownvoteColor(val value: Color?) : Intent
        data class ChangeReplyColor(val value: Color?) : Intent
        data class ChangeCrashReportEnabled(val value: Boolean) : Intent
        data class ChangeVoteFormat(val value: VoteFormat) : Intent
        data class ChangeAutoLoadImages(val value: Boolean) : Intent
        data class ChangeAutoExpandComments(val value: Boolean) : Intent
        data class ChangeFullHeightImages(val value: Boolean) : Intent
        data class ChangeHideNavigationBarWhileScrolling(val value: Boolean) : Intent
        data class ChangeZombieModeInterval(val value: Duration) : Intent
        data class ChangeZombieModeScrollAmount(val value: Float) : Intent
        data class ChangeMarkAsReadWhileScrolling(val value: Boolean) : Intent
        data class ChangeDefaultInboxUnreadOnly(val value: Boolean) : Intent
        data class ChangeSearchPostTitleOnly(val value: Boolean) : Intent
        data class ChangeEdgeToEdge(val value: Boolean) : Intent
        data class ChangePostBodyMaxLines(val value: Int) : Intent
        data class ChangeInfiniteScrollDisabled(val value: Boolean) : Intent
    }

    data class UiState(
        val isLogged: Boolean = false,
        val uiTheme: UiTheme? = null,
        val uiFontFamily: UiFontFamily = UiFontFamily.Poppins,
        val customSeedColor: Color? = null,
        val upVoteColor: Color? = null,
        val downVoteColor: Color? = null,
        val replyColor: Color? = null,
        val saveColor: Color? = null,
        val uiFontScale: FontScale = FontScale.Normal,
        val contentFontScale: FontScale = FontScale.Normal,
        val contentFontFamily: UiFontFamily = UiFontFamily.Poppins,
        val lang: String = "",
        val postLayout: PostLayout = PostLayout.Card,
        val defaultListingType: ListingType = ListingType.Local,
        val defaultPostSortType: SortType = SortType.Active,
        val defaultCommentSortType: SortType = SortType.New,
        val defaultInboxUnreadOnly: Boolean = true,
        val navBarTitlesVisible: Boolean = false,
        val supportsDynamicColors: Boolean = false,
        val dynamicColors: Boolean = false,
        val includeNsfw: Boolean = true,
        val blurNsfw: Boolean = true,
        val openUrlsInExternalBrowser: Boolean = false,
        val enableSwipeActions: Boolean = true,
        val enableDoubleTapAction: Boolean = true,
        val crashReportEnabled: Boolean = false,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = false,
        val autoExpandComments: Boolean = false,
        val fullHeightImages: Boolean = false,
        val hideNavigationBarWhileScrolling: Boolean = true,
        val zombieModeInterval: Duration = 2.5.seconds,
        val zombieModeScrollAmount: Float = 100f,
        val markAsReadWhileScrolling: Boolean = true,
        val availableSortTypesForPosts: List<SortType> = emptyList(),
        val availableSortTypesForComments: List<SortType> = emptyList(),
        val commentBarTheme: CommentBarTheme = CommentBarTheme.Blue,
        val searchPostTitleOnly: Boolean = false,
        val edgeToEdge: Boolean = true,
        val postBodyMaxLines: Int? = null,
        val infiniteScrollDisabled: Boolean = false,
        val opaqueSystemBars: Boolean = false,
    )

    sealed interface Effect
}
