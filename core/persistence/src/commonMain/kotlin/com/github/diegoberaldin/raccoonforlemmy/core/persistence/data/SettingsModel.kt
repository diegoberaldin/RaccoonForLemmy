package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class SettingsModel(
    val id: Long? = null,
    val theme: Int? = null,
    val uiFontFamily: Int = 0,
    val uiFontScale: Float = 1f,
    val contentFontScale: Float = 1f,
    val contentFontFamily: Int = 0,
    val locale: String? = null,
    val defaultListingType: Int = 2,
    val defaultPostSortType: Int = 1,
    val defaultInboxType: Int = 0,
    val defaultCommentSortType: Int = 3,
    val includeNsfw: Boolean = false,
    val blurNsfw: Boolean = true,
    val navigationTitlesVisible: Boolean = true,
    val dynamicColors: Boolean = false,
    val openUrlsInExternalBrowser: Boolean = true,
    val enableSwipeActions: Boolean = true,
    val enableDoubleTapAction: Boolean = false,
    val customSeedColor: Int? = null,
    val upVoteColor: Int? = null,
    val downVoteColor: Int? = null,
    val postLayout: Int = 0,
    val fullHeightImages: Boolean = true,
    val voteFormat: VoteFormat = VoteFormat.Aggregated,
    val autoLoadImages: Boolean = true,
    val autoExpandComments: Boolean = true,
    val hideNavigationBarWhileScrolling: Boolean = true,
    val zombieModeInterval: Duration = 1.seconds,
    val zombieModeScrollAmount: Float = 55f,
    val markAsReadWhileScrolling: Boolean = false,
    val commentBarTheme: Int = 0,
    val replyColor: Int? = null,
    val searchPostTitleOnly: Boolean = false,
    val edgeToEdge: Boolean = true,
    val postBodyMaxLines: Int? = null,
    val infiniteScrollEnabled: Boolean = true,
    val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = listOf(
        ActionOnSwipe.UpVote,
        ActionOnSwipe.Save,
    ),
    val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = listOf(
        ActionOnSwipe.DownVote,
        ActionOnSwipe.Reply,
    ),
    val actionsOnSwipeToStartComments: List<ActionOnSwipe> = listOf(
        ActionOnSwipe.UpVote,
        ActionOnSwipe.Save,
    ),
    val actionsOnSwipeToEndComments: List<ActionOnSwipe> = listOf(
        ActionOnSwipe.DownVote,
        ActionOnSwipe.Reply,
    ),
    val actionsOnSwipeToStartInbox: List<ActionOnSwipe> = listOf(
        ActionOnSwipe.UpVote,
        ActionOnSwipe.ToggleRead,
    ),
    val actionsOnSwipeToEndInbox: List<ActionOnSwipe> = listOf(
        ActionOnSwipe.DownVote,
    ),
)
