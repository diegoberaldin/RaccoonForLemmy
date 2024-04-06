package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import app.cash.sqldelight.Query
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.GetBy
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.SettingsQueries
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.entities.AppDatabase
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.DurationUnit

class DefaultSettingsRepositoryTest {

    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val query = mockk<Query<GetBy>>()
    private val queries = mockk<SettingsQueries>(relaxUnitFun = true) {
        every { getBy(any()) } returns query
    }
    private val provider = mockk<DatabaseProvider> {
        every { getDatabase() } returns mockk<AppDatabase> {
            every { settingsQueries } returns queries
        }
    }
    private val keyStore = mockk<TemporaryKeyStore>(relaxUnitFun = true)

    private val sut = DefaultSettingsRepository(
        provider = provider,
        keyStore = keyStore,
    )

    @Test
    fun givenAccount_whenGetSettings_thenResultIsAsExpected() = runTest {
        every { query.executeAsOneOrNull() } returns createFake(id = 2)

        val res = sut.getSettings(1)

        assertEquals(2, res.id)

        verify {
            queries.getBy(account_id = 1)
        }
    }

    @Test
    fun givenNoAccount_whenGetSettings_thenResultIsAsExpected() = runTest {
        every { keyStore[any(), any<Long>()] } returns 0
        every { keyStore[any(), any<Int>()] } returns 0
        every { keyStore[any(), any<Float>()] } returns 1f
        every { keyStore[any(), any<String>()] } returns ""
        every { keyStore[any(), any<Boolean>()] } returns false
        every { keyStore.containsKey(any()) } returns true

        val res = sut.getSettings(null)

        assertNotNull(res)

        verify {
            queries wasNot Called
        }
    }

    @Test
    fun whenChangeCurrentSettings_thenValueIsUpdated() = runTest {
        val model = SettingsModel(defaultListingType = 1)
        sut.changeCurrentSettings(model)
        val value = sut.currentSettings.value
        assertEquals(model, value)
    }

    @Test
    fun whenCreateSettings_thenResultIsAsExpected() = runTest {
        val model = SettingsModel()
        sut.createSettings(model, 1)

        verify {
            queries.create(
                theme = model.theme?.toLong(),
                uiFontScale = model.uiFontScale.toDouble(),
                uiFontFamily = model.uiFontFamily.toLong(),
                titleFontScale = model.contentFontScale.title.toDouble(),
                contentFontScale = model.contentFontScale.body.toDouble(),
                commentFontScale = model.contentFontScale.comment.toDouble(),
                ancillaryFontScale = model.contentFontScale.ancillary.toDouble(),
                locale = model.locale,
                defaultListingType = model.defaultListingType.toLong(),
                defaultPostSortType = model.defaultPostSortType.toLong(),
                defaultCommentSortType = model.defaultCommentSortType.toLong(),
                defaultInboxType = model.defaultInboxType.toLong(),
                includeNsfw = if (model.includeNsfw) 1 else 0,
                blurNsfw = if (model.blurNsfw) 1 else 0,
                navigationTitlesVisible = if (model.navigationTitlesVisible) 1 else 0,
                dynamicColors = if (model.dynamicColors) 1 else 0,
                openUrlsInExternalBrowser = if (model.openUrlsInExternalBrowser) 1 else 0,
                enableSwipeActions = if (model.enableSwipeActions) 1 else 0,
                enableDoubleTapAction = if (model.enableDoubleTapAction) 1 else 0,
                customSeedColor = model.customSeedColor?.toLong(),
                postLayout = model.postLayout.toLong(),
                separateUpAndDownVotes = if (model.voteFormat == VoteFormat.Separated) 1 else 0,
                autoLoadImages = if (model.autoLoadImages) 1 else 0,
                autoExpandComments = if (model.autoExpandComments) 1 else 0,
                fullHeightImages = if (model.fullHeightImages) 1 else 0,
                upvoteColor = model.upVoteColor?.toLong(),
                downvoteColor = model.downVoteColor?.toLong(),
                hideNavigationBarWhileScrolling = if (model.hideNavigationBarWhileScrolling) 1 else 0,
                zombieModeInterval = model.zombieModeInterval.toLong(DurationUnit.MILLISECONDS),
                zombieModeScrollAmount = model.zombieModeScrollAmount.toDouble(),
                markAsReadWhileScrolling = if (model.markAsReadWhileScrolling) 1 else 0,
                commentBarTheme = model.commentBarTheme.toLong(),
                replyColor = model.replyColor?.toLong(),
                saveColor = model.saveColor?.toLong(),
                searchPostTitleOnly = if (model.searchPostTitleOnly) 1 else 0,
                contentFontFamily = model.contentFontFamily.toLong(),
                edgeToEdge = if (model.edgeToEdge) 1 else 0,
                postBodyMaxLines = model.postBodyMaxLines?.toLong(),
                infiniteScrollEnabled = if (model.infiniteScrollEnabled) 1 else 0,
                actionsOnSwipeToStartPosts = model.actionsOnSwipeToStartPosts.serialized(),
                actionsOnSwipeToEndPosts = model.actionsOnSwipeToEndPosts.serialized(),
                actionsOnSwipeToStartComments = model.actionsOnSwipeToStartComments.serialized(),
                actionsOnSwipeToEndComments = model.actionsOnSwipeToEndComments.serialized(),
                actionsOnSwipeToStartInbox = model.actionsOnSwipeToStartInbox.serialized(),
                actionsOnSwipeToEndInbox = model.actionsOnSwipeToEndInbox.serialized(),
                opaqueSystemBars = if (model.opaqueSystemBars) 1 else 0,
                showScores = if (model.showScores) 1 else 0,
                preferUserNicknames = if (model.preferUserNicknames) 1 else 0,
                commentBarThickness = model.commentBarThickness.toLong(),
                imageSourcePath = if (model.imageSourcePath) 1 else 0,
                defaultExploreType = model.defaultExploreType.toLong(),
                defaultLanguageId = model.defaultLanguageId?.toLong(),
                account_id = 1,
            )
        }
    }

    @Test
    fun whenUpdate_thenResultIsAsExpected() = runTest {
        val model = SettingsModel(defaultListingType = 1)
        sut.updateSettings(model, 1)

        verify {
            queries.update(
                theme = model.theme?.toLong(),
                uiFontScale = model.uiFontScale.toDouble(),
                uiFontFamily = model.uiFontFamily.toLong(),
                titleFontScale = model.contentFontScale.title.toDouble(),
                contentFontScale = model.contentFontScale.body.toDouble(),
                commentFontScale = model.contentFontScale.comment.toDouble(),
                ancillaryFontScale = model.contentFontScale.ancillary.toDouble(),
                locale = model.locale,
                defaultListingType = model.defaultListingType.toLong(),
                defaultPostSortType = model.defaultPostSortType.toLong(),
                defaultCommentSortType = model.defaultCommentSortType.toLong(),
                defaultInboxType = model.defaultInboxType.toLong(),
                includeNsfw = if (model.includeNsfw) 1 else 0,
                blurNsfw = if (model.blurNsfw) 1 else 0,
                navigationTitlesVisible = if (model.navigationTitlesVisible) 1 else 0,
                dynamicColors = if (model.dynamicColors) 1 else 0,
                openUrlsInExternalBrowser = if (model.openUrlsInExternalBrowser) 1 else 0,
                enableSwipeActions = if (model.enableSwipeActions) 1 else 0,
                enableDoubleTapAction = if (model.enableDoubleTapAction) 1 else 0,
                customSeedColor = model.customSeedColor?.toLong(),
                postLayout = model.postLayout.toLong(),
                separateUpAndDownVotes = if (model.voteFormat == VoteFormat.Separated) 1 else 0,
                autoLoadImages = if (model.autoLoadImages) 1 else 0,
                autoExpandComments = if (model.autoExpandComments) 1 else 0,
                fullHeightImages = if (model.fullHeightImages) 1 else 0,
                upvoteColor = model.upVoteColor?.toLong(),
                downvoteColor = model.downVoteColor?.toLong(),
                hideNavigationBarWhileScrolling = if (model.hideNavigationBarWhileScrolling) 1 else 0,
                zombieModeInterval = model.zombieModeInterval.toLong(DurationUnit.MILLISECONDS),
                zombieModeScrollAmount = model.zombieModeScrollAmount.toDouble(),
                markAsReadWhileScrolling = if (model.markAsReadWhileScrolling) 1 else 0,
                commentBarTheme = model.commentBarTheme.toLong(),
                replyColor = model.replyColor?.toLong(),
                saveColor = model.saveColor?.toLong(),
                searchPostTitleOnly = if (model.searchPostTitleOnly) 1 else 0,
                contentFontFamily = model.contentFontFamily.toLong(),
                edgeToEdge = if (model.edgeToEdge) 1 else 0,
                postBodyMaxLines = model.postBodyMaxLines?.toLong(),
                infiniteScrollEnabled = if (model.infiniteScrollEnabled) 1 else 0,
                actionsOnSwipeToStartPosts = model.actionsOnSwipeToStartPosts.serialized(),
                actionsOnSwipeToEndPosts = model.actionsOnSwipeToEndPosts.serialized(),
                actionsOnSwipeToStartComments = model.actionsOnSwipeToStartComments.serialized(),
                actionsOnSwipeToEndComments = model.actionsOnSwipeToEndComments.serialized(),
                actionsOnSwipeToStartInbox = model.actionsOnSwipeToStartInbox.serialized(),
                actionsOnSwipeToEndInbox = model.actionsOnSwipeToEndInbox.serialized(),
                opaqueSystemBars = if (model.opaqueSystemBars) 1 else 0,
                showScores = if (model.showScores) 1 else 0,
                preferUserNicknames = if (model.preferUserNicknames) 1 else 0,
                commentBarThickness = model.commentBarThickness.toLong(),
                imageSourcePath = if (model.imageSourcePath) 1 else 0,
                defaultExploreType = model.defaultExploreType.toLong(),
                defaultLanguageId = model.defaultLanguageId?.toLong(),
                account_id = 1,
            )
        }
    }

    private fun List<ActionOnSwipe>.serialized(): String = map { it.toInt() }.joinToString(",")

    private fun createFake(
        id: Long = 0,
        theme: Long? = null,
        uiFontFamily: Long = 0,
        uiFontScale: Double = 1.0,
        contentFontScale: Double = 1.0,
        ancillaryFontScale: Double = 1.0,
        commentFontScale: Double = 1.0,
        titleFontScale: Double = 1.0,
        contentFontFamily: Long = 0,
        locale: String? = null,
        defaultListingType: Long = 2,
        defaultPostSortType: Long = 1,
        defaultInboxType: Long = 0,
        defaultCommentSortType: Long = 3,
        defaultExploreType: Long = 2,
        includeNsfw: Boolean = false,
        blurNsfw: Boolean = true,
        navigationTitlesVisible: Boolean = true,
        dynamicColors: Boolean = false,
        openUrlsInExternalBrowser: Boolean = true,
        enableSwipeActions: Boolean = true,
        enableDoubleTapAction: Boolean = false,
        customSeedColor: Long? = null,
        upVoteColor: Long? = null,
        downVoteColor: Long? = null,
        postLayout: Long = 0,
        fullHeightImages: Boolean = true,
        autoLoadImages: Boolean = true,
        autoExpandComments: Boolean = true,
        hideNavigationBarWhileScrolling: Boolean = true,
        zombieModeInterval: Long = 1,
        zombieModeScrollAmount: Double = 55.0,
        markAsReadWhileScrolling: Boolean = false,
        commentBarTheme: Long = 0,
        replyColor: Long? = null,
        saveColor: Long? = null,
        searchPostTitleOnly: Boolean = false,
        edgeToEdge: Boolean = true,
        postBodyMaxLines: Long? = null,
        infiniteScrollEnabled: Boolean = true,
        actionsOnSwipeToStartPosts: String = "",
        actionsOnSwipeToEndPosts: String = "",
        actionsOnSwipeToStartComments: String = "",
        actionsOnSwipeToEndComments: String = "",
        actionsOnSwipeToStartInbox: String = "",
        actionsOnSwipeToEndInbox: String = "",
        opaqueSystemBars: Boolean = false,
        showScores: Boolean = true,
        preferUserNicknames: Boolean = true,
        commentBarThickness: Long = 1,
        imageSourcePath: Boolean = false,
        separateUpAndDownVotes: Boolean = false,
        defaultLanguageId: Long? = null,
    ) = GetBy(
        id = id,
        theme = theme,
        uiFontFamily = uiFontFamily,
        uiFontScale = uiFontScale,
        contentFontScale = contentFontScale,
        contentFontFamily = contentFontFamily,
        locale = locale,
        defaultListingType = defaultListingType,
        defaultPostSortType = defaultPostSortType,
        defaultInboxType = defaultInboxType,
        defaultCommentSortType = defaultCommentSortType,
        defaultExploreType = defaultExploreType,
        includeNsfw = if (includeNsfw) 1 else 0,
        blurNsfw = if (blurNsfw) 1 else 0,
        navigationTitlesVisible = if (navigationTitlesVisible) 1 else 0,
        dynamicColors = if (dynamicColors) 1 else 0,
        openUrlsInExternalBrowser = if (openUrlsInExternalBrowser) 1 else 0,
        enableSwipeActions = if (enableSwipeActions) 1 else 0,
        enableDoubleTapAction = if (enableDoubleTapAction) 1 else 0,
        customSeedColor = customSeedColor,
        upvoteColor = upVoteColor,
        downvoteColor = downVoteColor,
        postLayout = postLayout,
        fullHeightImages = if (fullHeightImages) 1 else 0,
        autoLoadImages = if (autoLoadImages) 1 else 0,
        autoExpandComments = if (autoExpandComments) 1 else 0,
        hideNavigationBarWhileScrolling = if (hideNavigationBarWhileScrolling) 1 else 0,
        zombieModeInterval = zombieModeInterval,
        zombieModeScrollAmount = zombieModeScrollAmount,
        markAsReadWhileScrolling = if (markAsReadWhileScrolling) 1 else 0,
        commentBarTheme = commentBarTheme,
        replyColor = replyColor,
        saveColor = saveColor,
        searchPostTitleOnly = if (searchPostTitleOnly) 1 else 0,
        edgeToEdge = if (edgeToEdge) 1 else 0,
        postBodyMaxLines = postBodyMaxLines,
        infiniteScrollEnabled = if (infiniteScrollEnabled) 1 else 0,
        actionsOnSwipeToStartPosts = actionsOnSwipeToStartPosts,
        actionsOnSwipeToEndPosts = actionsOnSwipeToEndPosts,
        actionsOnSwipeToStartComments = actionsOnSwipeToStartComments,
        actionsOnSwipeToEndComments = actionsOnSwipeToEndComments,
        actionsOnSwipeToStartInbox = actionsOnSwipeToStartInbox,
        actionsOnSwipeToEndInbox = actionsOnSwipeToEndInbox,
        opaqueSystemBars = if (opaqueSystemBars) 1 else 0,
        showScores = if (showScores) 1 else 0,
        preferUserNicknames = if (preferUserNicknames) 1 else 0,
        commentBarThickness = commentBarThickness,
        imageSourcePath = if (imageSourcePath) 1 else 0,
        ancillaryFontScale = ancillaryFontScale,
        commentFontScale = commentFontScale,
        titleFontScale = titleFontScale,
        separateUpAndDownVotes = if (separateUpAndDownVotes) 1 else 0,
        defaultLanguageId = defaultLanguageId?.toLong(),
    )
}
