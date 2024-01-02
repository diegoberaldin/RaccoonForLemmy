package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toLong
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toVoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.GetBy
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

private object KeyStoreKeys {
    const val UiTheme = "uiTheme"
    const val UiFontFamily = "uiFontFamily"
    const val UiFontScale = "uiFontSize"
    const val ContentFontScale = "contentFontSize"
    const val Locale = "locale"
    const val DefaultListingType = "defaultListingType"
    const val DefaultPostSortType = "defaultPostSortType"
    const val DefaultInboxType = "defaultInboxType"
    const val DefaultCommentSortType = "defaultCommentSortType"
    const val IncludeNsfw = "includeNsfw"
    const val BlurNsfw = "blurNsfw"
    const val NavItemTitlesVisible = "navItemTitlesVisible"
    const val DynamicColors = "dynamicColors"
    const val OpenUrlsInExternalBrowser = "openUrlsInExternalBrowser"
    const val EnableSwipeActions = "enableSwipeActions"
    const val EnableDoubleTapAction = "enableDoubleTapAction"
    const val CustomSeedColor = "customPrimaryColor"
    const val PostLayout = "postLayout"
    const val SeparateUpAndDownVotes = "separateUpAndDownVotes"
    const val AutoLoadImages = "autoLoadImages"
    const val AutoExpandComments = "autoExpandComments"
    const val FullHeightImages = "fullHeightImages"
    const val UpvoteColor = "upvoteColor"
    const val DownVoteColor = "downVoteColor"
    const val HideNavigationBarWhileScrolling = "hideNavigationBarWhileScrolling"
    const val ZombieModeInterval = "zombieModeInterval"
    const val ZombieModeScrollAmount = "zombieModeScrollAmount"
    const val MarkAsReadWhileScrolling = "markAsReadWhileScrolling"
    const val CommentBarTheme = "commentBarTheme"
    const val ReplyColor = "replyColor"
    const val SearchPostTitleOnly = "searchPostTitleOnly"
    const val ContentFontFamily = "contentFontFamily"
    const val EdgeToEdge = "edgeToEdge"
    const val PostBodyMaxLines = "postBodyMaxLines"
    const val InfiniteScrollEnabled = "infiniteScrollEnabled"
}

internal class DefaultSettingsRepository(
    provider: DatabaseProvider,
    private val keyStore: TemporaryKeyStore,
) : SettingsRepository {

    private val db = provider.getDatabase()

    override val currentSettings = MutableStateFlow(SettingsModel())

    override suspend fun createSettings(settings: SettingsModel, accountId: Long) =
        withContext(Dispatchers.IO) {
            db.settingsQueries.create(
                theme = settings.theme?.toLong(),
                uiFontScale = settings.uiFontScale.toDouble(),
                uiFontFamily = settings.uiFontFamily.toLong(),
                contentFontScale = settings.contentFontScale.toDouble(),
                locale = settings.locale,
                defaultListingType = settings.defaultListingType.toLong(),
                defaultPostSortType = settings.defaultPostSortType.toLong(),
                defaultCommentSortType = settings.defaultCommentSortType.toLong(),
                defaultInboxType = settings.defaultInboxType.toLong(),
                includeNsfw = if (settings.includeNsfw) 1L else 0L,
                blurNsfw = if (settings.blurNsfw) 1L else 0L,
                navigationTitlesVisible = if (settings.navigationTitlesVisible) 1L else 0L,
                dynamicColors = if (settings.dynamicColors) 1L else 0L,
                openUrlsInExternalBrowser = if (settings.openUrlsInExternalBrowser) 1L else 0L,
                enableSwipeActions = if (settings.enableSwipeActions) 1L else 0L,
                enableDoubleTapAction = if (settings.enableDoubleTapAction) 1L else 0L,
                customSeedColor = settings.customSeedColor?.toLong(),
                account_id = accountId,
                postLayout = settings.postLayout.toLong(),
                separateUpAndDownVotes = settings.voteFormat.toLong(),
                autoLoadImages = if (settings.autoLoadImages) 1 else 0,
                autoExpandComments = if (settings.autoExpandComments) 1 else 0,
                fullHeightImages = if (settings.fullHeightImages) 1 else 0,
                upvoteColor = settings.upvoteColor?.toLong(),
                downvoteColor = settings.downvoteColor?.toLong(),
                hideNavigationBarWhileScrolling = if (settings.hideNavigationBarWhileScrolling) 1 else 0,
                zombieModeInterval = settings.zombieModeInterval.inWholeMilliseconds,
                zombieModeScrollAmount = settings.zombieModeScrollAmount.toDouble(),
                markAsReadWhileScrolling = if (settings.markAsReadWhileScrolling) 1 else 0,
                commentBarTheme = settings.commentBarTheme.toLong(),
                replyColor = settings.replyColor?.toLong(),
                searchPostTitleOnly = if (settings.searchPostTitleOnly) 1 else 0,
                contentFontFamily = settings.contentFontFamily.toLong(),
                edgeToEdge = if (settings.edgeToEdge) 1 else 0,
                postBodyMaxLines = settings.postBodyMaxLines?.toLong(),
                infiniteScrollEnabled = if (settings.infiniteScrollEnabled) 1 else 0,
            )
        }

    override suspend fun getSettings(accountId: Long?): SettingsModel =
        withContext(Dispatchers.IO) {
            if (accountId == null) {
                // anonymous user, reading from keystore
                SettingsModel(
                    theme = if (keyStore.containsKey(KeyStoreKeys.UiTheme)) {
                        keyStore[KeyStoreKeys.UiTheme, 0]
                    } else null,
                    uiFontScale = keyStore[KeyStoreKeys.UiFontScale, 1f],
                    uiFontFamily = keyStore[KeyStoreKeys.UiFontFamily, 0],
                    contentFontScale = keyStore[KeyStoreKeys.ContentFontScale, 1f],
                    locale = keyStore[KeyStoreKeys.Locale, ""].takeIf { it.isNotEmpty() },
                    defaultListingType = keyStore[KeyStoreKeys.DefaultListingType, 2],
                    defaultPostSortType = keyStore[KeyStoreKeys.DefaultPostSortType, 1],
                    defaultCommentSortType = keyStore[KeyStoreKeys.DefaultCommentSortType, 3],
                    defaultInboxType = keyStore[KeyStoreKeys.DefaultInboxType, 0],
                    includeNsfw = keyStore[KeyStoreKeys.IncludeNsfw, false],
                    blurNsfw = keyStore[KeyStoreKeys.BlurNsfw, true],
                    navigationTitlesVisible = keyStore[KeyStoreKeys.NavItemTitlesVisible, true],
                    dynamicColors = keyStore[KeyStoreKeys.DynamicColors, false],
                    openUrlsInExternalBrowser = keyStore[KeyStoreKeys.OpenUrlsInExternalBrowser, true],
                    enableSwipeActions = keyStore[KeyStoreKeys.EnableSwipeActions, true],
                    enableDoubleTapAction = keyStore[KeyStoreKeys.EnableDoubleTapAction, false],
                    customSeedColor = if (!keyStore.containsKey(KeyStoreKeys.CustomSeedColor)) null else keyStore[KeyStoreKeys.CustomSeedColor, 0],
                    postLayout = keyStore[KeyStoreKeys.PostLayout, 0],
                    voteFormat = keyStore[KeyStoreKeys.SeparateUpAndDownVotes, 0L].toVoteFormat(),
                    autoLoadImages = keyStore[KeyStoreKeys.AutoLoadImages, true],
                    autoExpandComments = keyStore[KeyStoreKeys.AutoExpandComments, true],
                    fullHeightImages = keyStore[KeyStoreKeys.FullHeightImages, true],
                    upvoteColor = if (!keyStore.containsKey(KeyStoreKeys.UpvoteColor)) null else keyStore[KeyStoreKeys.UpvoteColor, 0],
                    downvoteColor = if (!keyStore.containsKey(KeyStoreKeys.DownVoteColor)) null else keyStore[KeyStoreKeys.DownVoteColor, 0],
                    hideNavigationBarWhileScrolling = keyStore[KeyStoreKeys.HideNavigationBarWhileScrolling, true],
                    zombieModeInterval = keyStore[KeyStoreKeys.ZombieModeInterval, 1000].milliseconds,
                    zombieModeScrollAmount = keyStore[KeyStoreKeys.ZombieModeScrollAmount, 55f],
                    markAsReadWhileScrolling = keyStore[KeyStoreKeys.MarkAsReadWhileScrolling, false],
                    commentBarTheme = keyStore[KeyStoreKeys.CommentBarTheme, 0],
                    replyColor = if (!keyStore.containsKey(KeyStoreKeys.ReplyColor)) null else keyStore[KeyStoreKeys.ReplyColor, 0],
                    searchPostTitleOnly = keyStore[KeyStoreKeys.SearchPostTitleOnly, false],
                    contentFontFamily = keyStore[KeyStoreKeys.ContentFontFamily, 0],
                    edgeToEdge = keyStore[KeyStoreKeys.EdgeToEdge, true],
                    postBodyMaxLines = if (keyStore.containsKey(KeyStoreKeys.PostBodyMaxLines)) {
                        keyStore[KeyStoreKeys.PostBodyMaxLines, 0]
                    } else null,
                    infiniteScrollEnabled = keyStore[KeyStoreKeys.InfiniteScrollEnabled, true],
                )
            } else {
                val entity = db.settingsQueries.getBy(accountId).executeAsOneOrNull()
                val result = entity?.toModel()
                result ?: SettingsModel()
            }
        }

    override suspend fun updateSettings(settings: SettingsModel, accountId: Long?) =
        withContext(Dispatchers.IO) {
            if (accountId == null) {
                // anonymous user, storing into keystore
                if (settings.theme != null) {
                    keyStore.save(KeyStoreKeys.UiTheme, settings.theme)
                } else {
                    keyStore.remove(KeyStoreKeys.UiTheme)
                }
                keyStore.save(KeyStoreKeys.UiFontScale, settings.uiFontScale)
                keyStore.save(KeyStoreKeys.UiFontFamily, settings.uiFontFamily)
                keyStore.save(KeyStoreKeys.ContentFontScale, settings.contentFontScale)
                if (!settings.locale.isNullOrEmpty()) {
                    keyStore.save(KeyStoreKeys.Locale, settings.locale)
                } else {
                    keyStore.remove(KeyStoreKeys.Locale)
                }
                keyStore.save(KeyStoreKeys.DefaultListingType, settings.defaultListingType)
                keyStore.save(KeyStoreKeys.DefaultPostSortType, settings.defaultPostSortType)
                keyStore.save(KeyStoreKeys.DefaultCommentSortType, settings.defaultCommentSortType)
                keyStore.save(KeyStoreKeys.DefaultInboxType, settings.defaultInboxType)
                keyStore.save(KeyStoreKeys.IncludeNsfw, settings.includeNsfw)
                keyStore.save(KeyStoreKeys.BlurNsfw, settings.blurNsfw)
                keyStore.save(KeyStoreKeys.NavItemTitlesVisible, settings.navigationTitlesVisible)
                keyStore.save(KeyStoreKeys.DynamicColors, settings.dynamicColors)
                keyStore.save(
                    key = KeyStoreKeys.OpenUrlsInExternalBrowser,
                    value = settings.openUrlsInExternalBrowser
                )
                keyStore.save(KeyStoreKeys.EnableSwipeActions, settings.enableSwipeActions)
                keyStore.save(KeyStoreKeys.EnableDoubleTapAction, settings.enableDoubleTapAction)
                if (settings.customSeedColor != null) {
                    keyStore.save(KeyStoreKeys.CustomSeedColor, settings.customSeedColor)
                } else {
                    keyStore.remove(KeyStoreKeys.CustomSeedColor)
                }
                keyStore.save(KeyStoreKeys.PostLayout, settings.postLayout)
                keyStore.save(KeyStoreKeys.SeparateUpAndDownVotes, settings.voteFormat.toLong())
                keyStore.save(KeyStoreKeys.AutoLoadImages, settings.autoLoadImages)
                keyStore.save(KeyStoreKeys.AutoExpandComments, settings.autoExpandComments)
                keyStore.save(KeyStoreKeys.FullHeightImages, settings.fullHeightImages)
                if (settings.upvoteColor != null) {
                    keyStore.save(KeyStoreKeys.UpvoteColor, settings.upvoteColor)
                } else {
                    keyStore.remove(KeyStoreKeys.UpvoteColor)
                }
                if (settings.downvoteColor != null) {
                    keyStore.save(KeyStoreKeys.DownVoteColor, settings.downvoteColor)
                } else {
                    keyStore.remove(KeyStoreKeys.DownVoteColor)
                }
                keyStore.save(
                    KeyStoreKeys.HideNavigationBarWhileScrolling,
                    settings.hideNavigationBarWhileScrolling
                )
                keyStore.save(
                    KeyStoreKeys.ZombieModeInterval,
                    settings.zombieModeInterval.inWholeMilliseconds,
                )
                keyStore.save(
                    KeyStoreKeys.ZombieModeScrollAmount,
                    settings.zombieModeScrollAmount,
                )
                keyStore.save(
                    KeyStoreKeys.MarkAsReadWhileScrolling,
                    settings.markAsReadWhileScrolling,
                )
                keyStore.save(KeyStoreKeys.CommentBarTheme, settings.commentBarTheme)
                if (settings.replyColor != null) {
                    keyStore.save(KeyStoreKeys.ReplyColor, settings.replyColor)
                } else {
                    keyStore.remove(KeyStoreKeys.ReplyColor)
                }
                keyStore.save(
                    KeyStoreKeys.SearchPostTitleOnly,
                    settings.searchPostTitleOnly,
                )
                keyStore.save(KeyStoreKeys.ContentFontFamily, settings.contentFontFamily)
                keyStore.save(KeyStoreKeys.EdgeToEdge, settings.edgeToEdge)
                if (settings.postBodyMaxLines != null) {
                    keyStore.save(KeyStoreKeys.PostBodyMaxLines, settings.postBodyMaxLines)
                } else {
                    keyStore.remove(KeyStoreKeys.PostBodyMaxLines)
                }
                keyStore.save(KeyStoreKeys.InfiniteScrollEnabled, settings.infiniteScrollEnabled)
            } else {
                db.settingsQueries.update(
                    theme = settings.theme?.toLong(),
                    uiFontScale = settings.uiFontScale.toDouble(),
                    uiFontFamily = settings.uiFontFamily.toLong(),
                    contentFontScale = settings.contentFontScale.toDouble(),
                    locale = settings.locale,
                    defaultListingType = settings.defaultListingType.toLong(),
                    defaultPostSortType = settings.defaultPostSortType.toLong(),
                    defaultCommentSortType = settings.defaultCommentSortType.toLong(),
                    defaultInboxType = settings.defaultInboxType.toLong(),
                    includeNsfw = if (settings.includeNsfw) 1L else 0L,
                    blurNsfw = if (settings.blurNsfw) 1L else 0L,
                    navigationTitlesVisible = if (settings.navigationTitlesVisible) 1L else 0L,
                    dynamicColors = if (settings.dynamicColors) 1L else 0L,
                    openUrlsInExternalBrowser = if (settings.openUrlsInExternalBrowser) 1L else 0L,
                    enableSwipeActions = if (settings.enableSwipeActions) 1L else 0L,
                    enableDoubleTapAction = if (settings.enableDoubleTapAction) 1L else 0L,
                    customSeedColor = settings.customSeedColor?.toLong(),
                    postLayout = settings.postLayout.toLong(),
                    separateUpAndDownVotes = settings.voteFormat.toLong(),
                    autoLoadImages = if (settings.autoLoadImages) 1L else 0L,
                    autoExpandComments = if (settings.autoExpandComments) 1L else 0L,
                    fullHeightImages = if (settings.fullHeightImages) 1L else 0L,
                    account_id = accountId,
                    upvoteColor = settings.upvoteColor?.toLong(),
                    downvoteColor = settings.downvoteColor?.toLong(),
                    hideNavigationBarWhileScrolling = if (settings.hideNavigationBarWhileScrolling) 1L else 0L,
                    zombieModeInterval = settings.zombieModeInterval.inWholeMilliseconds,
                    zombieModeScrollAmount = settings.zombieModeScrollAmount.toDouble(),
                    markAsReadWhileScrolling = if (settings.markAsReadWhileScrolling) 1L else 0L,
                    commentBarTheme = settings.commentBarTheme.toLong(),
                    replyColor = settings.replyColor?.toLong(),
                    searchPostTitleOnly = if (settings.searchPostTitleOnly) 1L else 0L,
                    contentFontFamily = settings.contentFontFamily.toLong(),
                    edgeToEdge = if (settings.edgeToEdge) 1L else 0L,
                    postBodyMaxLines = settings.postBodyMaxLines?.toLong(),
                    infiniteScrollEnabled = if (settings.infiniteScrollEnabled) 1L else 0L,
                )
            }
        }

    override fun changeCurrentSettings(settings: SettingsModel) {
        currentSettings.value = settings
    }
}

private fun GetBy.toModel() = SettingsModel(
    id = id,
    theme = theme?.toInt(),
    uiFontScale = uiFontScale.toFloat(),
    uiFontFamily = uiFontFamily.toInt(),
    contentFontScale = contentFontScale.toFloat(),
    locale = locale,
    defaultListingType = defaultListingType.toInt(),
    defaultPostSortType = defaultPostSortType.toInt(),
    defaultCommentSortType = defaultCommentSortType.toInt(),
    defaultInboxType = defaultInboxType.toInt(),
    includeNsfw = includeNsfw != 0L,
    blurNsfw = blurNsfw != 0L,
    navigationTitlesVisible = navigationTitlesVisible != 0L,
    dynamicColors = dynamicColors != 0L,
    openUrlsInExternalBrowser = openUrlsInExternalBrowser != 0L,
    enableSwipeActions = enableSwipeActions != 0L,
    enableDoubleTapAction = enableDoubleTapAction != 0L,
    customSeedColor = customSeedColor?.toInt(),
    postLayout = postLayout.toInt(),
    voteFormat = separateUpAndDownVotes.toVoteFormat(),
    autoLoadImages = autoLoadImages != 0L,
    autoExpandComments = autoExpandComments != 0L,
    fullHeightImages = fullHeightImages != 0L,
    upvoteColor = upvoteColor?.toInt(),
    downvoteColor = downvoteColor?.toInt(),
    hideNavigationBarWhileScrolling = hideNavigationBarWhileScrolling != 0L,
    zombieModeInterval = zombieModeInterval.milliseconds,
    zombieModeScrollAmount = zombieModeScrollAmount.toFloat(),
    markAsReadWhileScrolling = markAsReadWhileScrolling != 0L,
    commentBarTheme = commentBarTheme.toInt(),
    replyColor = replyColor?.toInt(),
    searchPostTitleOnly = searchPostTitleOnly != 0L,
    contentFontFamily = contentFontFamily.toInt(),
    edgeToEdge = edgeToEdge != 0L,
    postBodyMaxLines = postBodyMaxLines?.toInt(),
    infiniteScrollEnabled = edgeToEdge != 0L,
)
