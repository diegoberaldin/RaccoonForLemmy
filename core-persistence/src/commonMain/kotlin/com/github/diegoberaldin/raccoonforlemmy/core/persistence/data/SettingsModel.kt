package com.github.diegoberaldin.raccoonforlemmy.core.persistence.data

import com.github.diegoberaldin.raccoonforlemmy.core.utils.JavaSerializable

data class SettingsModel(
    val id: Long? = null,
    val theme: Int? = null,
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
    val customSeedColor: Int? = null,
    val postLayout: Int = 0,
    val separateUpAndDownVotes: Boolean = false,
    val autoLoadImages: Boolean = true,
) : JavaSerializable
