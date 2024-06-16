package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontScales
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class SettingsModel(
    val id: Long? = null,
    val theme: Int? = null,
    val uiFontFamily: Int = 0,
    val uiFontScale: Float = 1f,
    val contentFontScale: ContentFontScales = ContentFontScales(),
    val contentFontFamily: Int = 0,
    val locale: String? = null,
    val defaultListingType: Int = 2,
    val defaultPostSortType: Int = 1,
    val defaultInboxType: Int = 0,
    val defaultCommentSortType: Int = 3,
    val defaultExploreType: Int = 2,
    val includeNsfw: Boolean = false,
    val blurNsfw: Boolean = true,
    val navigationTitlesVisible: Boolean = true,
    val dynamicColors: Boolean = false,
    val urlOpeningMode: Int = 1,
    val enableSwipeActions: Boolean = true,
    val enableDoubleTapAction: Boolean = false,
    val customSeedColor: Int? = null,
    val upVoteColor: Int? = null,
    val downVoteColor: Int? = null,
    val postLayout: Int = 0,
    val fullHeightImages: Boolean = true,
    val fullWidthImages: Boolean = false,
    val voteFormat: VoteFormat = VoteFormat.Aggregated,
    val autoLoadImages: Boolean = true,
    val autoExpandComments: Boolean = true,
    val hideNavigationBarWhileScrolling: Boolean = true,
    val zombieModeInterval: Duration = 1.seconds,
    val zombieModeScrollAmount: Float = 55f,
    val markAsReadWhileScrolling: Boolean = false,
    val commentBarTheme: Int = 0,
    val replyColor: Int? = null,
    val saveColor: Int? = null,
    val searchPostTitleOnly: Boolean = false,
    val edgeToEdge: Boolean = true,
    val postBodyMaxLines: Int? = null,
    val infiniteScrollEnabled: Boolean = true,
    val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = ActionOnSwipe.DEFAULT_SWIPE_TO_START_POSTS,
    val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = ActionOnSwipe.DEFAULT_SWIPE_TO_END_POSTS,
    val actionsOnSwipeToStartComments: List<ActionOnSwipe> = ActionOnSwipe.DEFAULT_SWIPE_TO_START_COMMENTS,
    val actionsOnSwipeToEndComments: List<ActionOnSwipe> = ActionOnSwipe.DEFAULT_SWIPE_TO_END_COMMENTS,
    val actionsOnSwipeToStartInbox: List<ActionOnSwipe> = ActionOnSwipe.DEFAULT_SWIPE_TO_START_INBOX,
    val actionsOnSwipeToEndInbox: List<ActionOnSwipe> = ActionOnSwipe.DEFAULT_SWIPE_TO_END_INBOX,
    val opaqueSystemBars: Boolean = false,
    val showScores: Boolean = true,
    val preferUserNicknames: Boolean = true,
    val commentBarThickness: Int = 1,
    val imageSourcePath: Boolean = false,
    val defaultLanguageId: Long? = null,
    val inboxBackgroundCheckPeriod: Duration? = null,
    val fadeReadPosts: Boolean = false,
    val showUnreadComments: Boolean = false,
    val enableButtonsToScrollBetweenComments: Boolean = false,
    val commentIndentAmount: Int = 2,
    val enableToggleFavoriteInNavDrawer: Boolean = false,
)
