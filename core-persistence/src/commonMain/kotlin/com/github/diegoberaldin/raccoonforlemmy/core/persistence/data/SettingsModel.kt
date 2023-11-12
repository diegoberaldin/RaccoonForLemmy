package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class SettingsModel(
    val id: Long? = null,
    val theme: Int? = null,
    val uiFontFamily: Int = 0,
    val uiFontScale: Float = 1f,
    val contentFontScale: Float = 1f,
    val locale: String? = null,
    val defaultListingType: Int = 0,
    val defaultPostSortType: Int = 0,
    val defaultCommentSortType: Int = 3,
    val includeNsfw: Boolean = true,
    val blurNsfw: Boolean = true,
    val navigationTitlesVisible: Boolean = true,
    val dynamicColors: Boolean = false,
    val openUrlsInExternalBrowser: Boolean = false,
    val enableSwipeActions: Boolean = true,
    val enableDoubleTapAction: Boolean = false,
    val customSeedColor: Int? = null,
    val upvoteColor: Int? = null,
    val downvoteColor: Int? = null,
    val postLayout: Int = 0,
    val fullHeightImages: Boolean = true,
    val separateUpAndDownVotes: Boolean = false,
    val autoLoadImages: Boolean = true,
    val autoExpandComments: Boolean = true,
    val hideNavigationBarWhileScrolling: Boolean = true,
    val zombieModeInterval: Duration = 2.seconds,
    val zombieModeScrollAmount: Float = 100f,
) : JavaSerializable
