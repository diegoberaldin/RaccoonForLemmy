package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toLong
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toVoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontScales
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.GetBy
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.toActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

private object KeyStoreKeys {
    const val UI_THEME = "uiTheme"
    const val UI_FONT_FAMILY = "uiFontFamily"
    const val UI_FONT_SCALE = "uiFontSize"
    const val CONTENT_TITLE_FONT_SCALE = "titleFontSize"
    const val CONTENT_BODY_FONT_SCALE = "contentFontSize"
    const val CONTENT_COMMENT_FONT_SCALE = "commentFontSize"
    const val CONTENT_ANCILLARY_FONT_SCALE = "ancillaryFontSize"
    const val LOCALE = "locale"
    const val DEFAULT_LISTING_TYPE = "defaultListingType"
    const val DEFAULT_POST_SORT_TYPE = "defaultPostSortType"
    const val DEFAULT_INBOX_TYPE = "defaultInboxType"
    const val DEFAULT_COMMENT_SORT_TYPE = "defaultCommentSortType"
    const val DEFAULT_EXPLORE_TYPE = "defaultExploreType"
    const val INCLUDE_NSFW = "includeNsfw"
    const val BLUR_NSFW = "blurNsfw"
    const val NAV_ITEM_TITLES_VISIBLE = "navItemTitlesVisible"
    const val DYNAMIC_COLORS = "dynamicColors"
    const val OPEN_URLS_IN_EXTERNAL_BROWSER = "openUrlsInExternalBrowser"
    const val ENABLE_SWIPE_ACTIONS = "enableSwipeActions"
    const val ENABLE_DOUBLE_TAP_ACTION = "enableDoubleTapAction"
    const val CUSTOM_SEED_COLOR = "customPrimaryColor"
    const val POST_LAYOUT = "postLayout"
    const val SEPARATE_UP_AND_DOWN_VOTES = "separateUpAndDownVotes"
    const val AUTO_LOAD_IMAGES = "autoLoadImages"
    const val AUTO_EXPAND_COMMENTS = "autoExpandComments"
    const val FULL_HEIGHT_IMAGES = "fullHeightImages"
    const val HIDE_NAVIGATION_BAR_WHILE_SCROLLING = "hideNavigationBarWhileScrolling"
    const val ZOMBIE_MODE_INTERVAL = "zombieModeInterval"
    const val ZOMBIE_MODE_SCROLL_AMOUNT = "zombieModeScrollAmount"
    const val MARK_AS_READ_WHILE_SCROLLING = "markAsReadWhileScrolling"
    const val COMMENT_BAR_THEME = "commentBarTheme"
    const val SEARCH_POST_TITLE_ONLY = "searchPostTitleOnly"
    const val CONTENT_FONT_FAMILY = "contentFontFamily"
    const val EDGE_TO_EDGE = "edgeToEdge"
    const val POST_BODY_MAX_LINES = "postBodyMaxLines"
    const val INFINITE_SCROLL_ENABLED = "infiniteScrollEnabled"
    const val OPAQUE_SYSTEM_BARS = "opaqueSystemBars"
    const val SHOW_SCORES = "showScores"
    const val PREFER_USER_NICKNAMES = "preferUserNicknames"
    const val COMMENT_BAR_THICKNESS = "commentBarThickness"
    const val IMAGE_SOURCE_PATH = "imageSourcePath"
    const val DEFAULT_LANGUAGE_ID = "defaultLanguageId"
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
                titleFontScale = settings.contentFontScale.title.toDouble(),
                contentFontScale = settings.contentFontScale.body.toDouble(),
                commentFontScale = settings.contentFontScale.comment.toDouble(),
                ancillaryFontScale = settings.contentFontScale.ancillary.toDouble(),
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
                upvoteColor = settings.upVoteColor?.toLong(),
                downvoteColor = settings.downVoteColor?.toLong(),
                hideNavigationBarWhileScrolling = if (settings.hideNavigationBarWhileScrolling) 1 else 0,
                zombieModeInterval = settings.zombieModeInterval.inWholeMilliseconds,
                zombieModeScrollAmount = settings.zombieModeScrollAmount.toDouble(),
                markAsReadWhileScrolling = if (settings.markAsReadWhileScrolling) 1 else 0,
                commentBarTheme = settings.commentBarTheme.toLong(),
                replyColor = settings.replyColor?.toLong(),
                saveColor = settings.saveColor?.toLong(),
                searchPostTitleOnly = if (settings.searchPostTitleOnly) 1 else 0,
                contentFontFamily = settings.contentFontFamily.toLong(),
                edgeToEdge = if (settings.edgeToEdge) 1 else 0,
                postBodyMaxLines = settings.postBodyMaxLines?.toLong(),
                infiniteScrollEnabled = if (settings.infiniteScrollEnabled) 1 else 0,
                actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts.map { it.toInt() }
                    .joinToString(","),
                actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts.map { it.toInt() }
                    .joinToString(","),
                actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments.map { it.toInt() }
                    .joinToString(","),
                actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments.map { it.toInt() }
                    .joinToString(","),
                actionsOnSwipeToStartInbox = settings.actionsOnSwipeToStartInbox.map { it.toInt() }
                    .joinToString(","),
                actionsOnSwipeToEndInbox = settings.actionsOnSwipeToEndInbox.map { it.toInt() }
                    .joinToString(","),
                opaqueSystemBars = if (settings.opaqueSystemBars) 1L else 0L,
                showScores = if (settings.showScores) 1L else 0L,
                preferUserNicknames = if (settings.preferUserNicknames) 1L else 0L,
                commentBarThickness = settings.commentBarThickness.toLong(),
                imageSourcePath = if (settings.imageSourcePath) 1L else 0L,
                defaultExploreType = settings.defaultExploreType.toLong(),
                defaultLanguageId = settings.defaultLanguageId?.toLong(),
            )
        }

    override suspend fun getSettings(accountId: Long?): SettingsModel =
        withContext(Dispatchers.IO) {
            if (accountId == null) {
                // anonymous user, reading from keystore
                val contentFontScale = ContentFontScales(
                    title = keyStore[KeyStoreKeys.CONTENT_TITLE_FONT_SCALE, 1f],
                    body = keyStore[KeyStoreKeys.CONTENT_BODY_FONT_SCALE, 1f],
                    comment = keyStore[KeyStoreKeys.CONTENT_COMMENT_FONT_SCALE, 1f],
                    ancillary = keyStore[KeyStoreKeys.CONTENT_ANCILLARY_FONT_SCALE, 1f],
                )
                SettingsModel(
                    theme = if (keyStore.containsKey(KeyStoreKeys.UI_THEME)) {
                        keyStore[KeyStoreKeys.UI_THEME, 0]
                    } else null,
                    uiFontScale = keyStore[KeyStoreKeys.UI_FONT_SCALE, 1f],
                    uiFontFamily = keyStore[KeyStoreKeys.UI_FONT_FAMILY, 0],
                    contentFontScale = contentFontScale,
                    locale = keyStore[KeyStoreKeys.LOCALE, ""].takeIf { it.isNotEmpty() },
                    defaultListingType = keyStore[KeyStoreKeys.DEFAULT_LISTING_TYPE, 2],
                    defaultPostSortType = keyStore[KeyStoreKeys.DEFAULT_POST_SORT_TYPE, 1],
                    defaultCommentSortType = keyStore[KeyStoreKeys.DEFAULT_COMMENT_SORT_TYPE, 3],
                    defaultInboxType = keyStore[KeyStoreKeys.DEFAULT_INBOX_TYPE, 0],
                    defaultExploreType = keyStore[KeyStoreKeys.DEFAULT_EXPLORE_TYPE, 2],
                    includeNsfw = keyStore[KeyStoreKeys.INCLUDE_NSFW, false],
                    blurNsfw = keyStore[KeyStoreKeys.BLUR_NSFW, true],
                    navigationTitlesVisible = keyStore[KeyStoreKeys.NAV_ITEM_TITLES_VISIBLE, true],
                    dynamicColors = keyStore[KeyStoreKeys.DYNAMIC_COLORS, false],
                    openUrlsInExternalBrowser = keyStore[KeyStoreKeys.OPEN_URLS_IN_EXTERNAL_BROWSER, true],
                    enableSwipeActions = keyStore[KeyStoreKeys.ENABLE_SWIPE_ACTIONS, true],
                    enableDoubleTapAction = keyStore[KeyStoreKeys.ENABLE_DOUBLE_TAP_ACTION, false],
                    customSeedColor = if (!keyStore.containsKey(KeyStoreKeys.CUSTOM_SEED_COLOR)) {
                        null
                    } else {
                        keyStore[KeyStoreKeys.CUSTOM_SEED_COLOR, 0]
                    },
                    postLayout = keyStore[KeyStoreKeys.POST_LAYOUT, 0],
                    voteFormat = keyStore[KeyStoreKeys.SEPARATE_UP_AND_DOWN_VOTES, 0L].toVoteFormat(),
                    autoLoadImages = keyStore[KeyStoreKeys.AUTO_LOAD_IMAGES, true],
                    autoExpandComments = keyStore[KeyStoreKeys.AUTO_EXPAND_COMMENTS, true],
                    fullHeightImages = keyStore[KeyStoreKeys.FULL_HEIGHT_IMAGES, true],
                    hideNavigationBarWhileScrolling = keyStore[KeyStoreKeys.HIDE_NAVIGATION_BAR_WHILE_SCROLLING, true],
                    zombieModeInterval = keyStore[KeyStoreKeys.ZOMBIE_MODE_INTERVAL, 1000].milliseconds,
                    zombieModeScrollAmount = keyStore[KeyStoreKeys.ZOMBIE_MODE_SCROLL_AMOUNT, 55f],
                    markAsReadWhileScrolling = keyStore[KeyStoreKeys.MARK_AS_READ_WHILE_SCROLLING, false],
                    commentBarTheme = keyStore[KeyStoreKeys.COMMENT_BAR_THEME, 0],
                    searchPostTitleOnly = keyStore[KeyStoreKeys.SEARCH_POST_TITLE_ONLY, false],
                    contentFontFamily = keyStore[KeyStoreKeys.CONTENT_FONT_FAMILY, 0],
                    edgeToEdge = keyStore[KeyStoreKeys.EDGE_TO_EDGE, true],
                    postBodyMaxLines = if (keyStore.containsKey(KeyStoreKeys.POST_BODY_MAX_LINES)) {
                        keyStore[KeyStoreKeys.POST_BODY_MAX_LINES, 0]
                    } else null,
                    infiniteScrollEnabled = keyStore[KeyStoreKeys.INFINITE_SCROLL_ENABLED, true],
                    opaqueSystemBars = keyStore[KeyStoreKeys.OPAQUE_SYSTEM_BARS, false],
                    showScores = keyStore[KeyStoreKeys.SHOW_SCORES, true],
                    preferUserNicknames = keyStore[KeyStoreKeys.PREFER_USER_NICKNAMES, true],
                    commentBarThickness = keyStore[KeyStoreKeys.COMMENT_BAR_THICKNESS, 1],
                    imageSourcePath = keyStore[KeyStoreKeys.IMAGE_SOURCE_PATH, false],
                    defaultLanguageId = if (keyStore.containsKey(KeyStoreKeys.DEFAULT_LANGUAGE_ID)) {
                        keyStore[KeyStoreKeys.DEFAULT_LANGUAGE_ID, 0]
                    } else null,
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
                    keyStore.save(KeyStoreKeys.UI_THEME, settings.theme)
                } else {
                    keyStore.remove(KeyStoreKeys.UI_THEME)
                }
                keyStore.save(KeyStoreKeys.UI_FONT_SCALE, settings.uiFontScale)
                keyStore.save(KeyStoreKeys.UI_FONT_FAMILY, settings.uiFontFamily)
                keyStore.save(
                    KeyStoreKeys.CONTENT_TITLE_FONT_SCALE,
                    settings.contentFontScale.title
                )
                keyStore.save(KeyStoreKeys.CONTENT_BODY_FONT_SCALE, settings.contentFontScale.body)
                keyStore.save(
                    KeyStoreKeys.CONTENT_COMMENT_FONT_SCALE,
                    settings.contentFontScale.comment
                )
                keyStore.save(
                    KeyStoreKeys.CONTENT_ANCILLARY_FONT_SCALE,
                    settings.contentFontScale.ancillary
                )
                if (!settings.locale.isNullOrEmpty()) {
                    keyStore.save(KeyStoreKeys.LOCALE, settings.locale)
                } else {
                    keyStore.remove(KeyStoreKeys.LOCALE)
                }
                keyStore.save(KeyStoreKeys.DEFAULT_LISTING_TYPE, settings.defaultListingType)
                keyStore.save(KeyStoreKeys.DEFAULT_POST_SORT_TYPE, settings.defaultPostSortType)
                keyStore.save(
                    KeyStoreKeys.DEFAULT_COMMENT_SORT_TYPE,
                    settings.defaultCommentSortType
                )
                keyStore.save(KeyStoreKeys.DEFAULT_INBOX_TYPE, settings.defaultInboxType)
                keyStore.save(KeyStoreKeys.DEFAULT_EXPLORE_TYPE, settings.defaultExploreType)
                keyStore.save(KeyStoreKeys.INCLUDE_NSFW, settings.includeNsfw)
                keyStore.save(KeyStoreKeys.BLUR_NSFW, settings.blurNsfw)
                keyStore.save(
                    KeyStoreKeys.NAV_ITEM_TITLES_VISIBLE,
                    settings.navigationTitlesVisible
                )
                keyStore.save(KeyStoreKeys.DYNAMIC_COLORS, settings.dynamicColors)
                keyStore.save(
                    key = KeyStoreKeys.OPEN_URLS_IN_EXTERNAL_BROWSER,
                    value = settings.openUrlsInExternalBrowser
                )
                keyStore.save(KeyStoreKeys.ENABLE_SWIPE_ACTIONS, settings.enableSwipeActions)
                keyStore.save(KeyStoreKeys.ENABLE_DOUBLE_TAP_ACTION, settings.enableDoubleTapAction)
                if (settings.customSeedColor != null) {
                    keyStore.save(KeyStoreKeys.CUSTOM_SEED_COLOR, settings.customSeedColor)
                } else {
                    keyStore.remove(KeyStoreKeys.CUSTOM_SEED_COLOR)
                }
                keyStore.save(KeyStoreKeys.POST_LAYOUT, settings.postLayout)
                keyStore.save(KeyStoreKeys.SEPARATE_UP_AND_DOWN_VOTES, settings.voteFormat.toLong())
                keyStore.save(KeyStoreKeys.AUTO_LOAD_IMAGES, settings.autoLoadImages)
                keyStore.save(KeyStoreKeys.AUTO_EXPAND_COMMENTS, settings.autoExpandComments)
                keyStore.save(KeyStoreKeys.FULL_HEIGHT_IMAGES, settings.fullHeightImages)
                keyStore.save(
                    KeyStoreKeys.HIDE_NAVIGATION_BAR_WHILE_SCROLLING,
                    settings.hideNavigationBarWhileScrolling
                )
                keyStore.save(
                    KeyStoreKeys.ZOMBIE_MODE_INTERVAL,
                    settings.zombieModeInterval.inWholeMilliseconds,
                )
                keyStore.save(
                    KeyStoreKeys.ZOMBIE_MODE_SCROLL_AMOUNT,
                    settings.zombieModeScrollAmount,
                )
                keyStore.save(
                    KeyStoreKeys.MARK_AS_READ_WHILE_SCROLLING,
                    settings.markAsReadWhileScrolling,
                )
                keyStore.save(KeyStoreKeys.COMMENT_BAR_THEME, settings.commentBarTheme)
                keyStore.save(
                    KeyStoreKeys.SEARCH_POST_TITLE_ONLY,
                    settings.searchPostTitleOnly,
                )
                keyStore.save(KeyStoreKeys.CONTENT_FONT_FAMILY, settings.contentFontFamily)
                keyStore.save(KeyStoreKeys.EDGE_TO_EDGE, settings.edgeToEdge)
                if (settings.postBodyMaxLines != null) {
                    keyStore.save(KeyStoreKeys.POST_BODY_MAX_LINES, settings.postBodyMaxLines)
                } else {
                    keyStore.remove(KeyStoreKeys.POST_BODY_MAX_LINES)
                }
                keyStore.save(KeyStoreKeys.INFINITE_SCROLL_ENABLED, settings.infiniteScrollEnabled)
                keyStore.save(KeyStoreKeys.OPAQUE_SYSTEM_BARS, settings.opaqueSystemBars)
                keyStore.save(KeyStoreKeys.SHOW_SCORES, settings.showScores)
                keyStore.save(KeyStoreKeys.PREFER_USER_NICKNAMES, settings.preferUserNicknames)
                keyStore.save(KeyStoreKeys.COMMENT_BAR_THICKNESS, settings.commentBarThickness)
                keyStore.save(KeyStoreKeys.IMAGE_SOURCE_PATH, settings.imageSourcePath)
                if (settings.defaultLanguageId != null) {
                    keyStore.save(KeyStoreKeys.DEFAULT_LANGUAGE_ID, settings.defaultLanguageId)
                } else {
                    keyStore.remove(KeyStoreKeys.DEFAULT_LANGUAGE_ID)
                }
            } else {
                db.settingsQueries.update(
                    theme = settings.theme?.toLong(),
                    uiFontScale = settings.uiFontScale.toDouble(),
                    uiFontFamily = settings.uiFontFamily.toLong(),
                    titleFontScale = settings.contentFontScale.title.toDouble(),
                    contentFontScale = settings.contentFontScale.body.toDouble(),
                    commentFontScale = settings.contentFontScale.comment.toDouble(),
                    ancillaryFontScale = settings.contentFontScale.ancillary.toDouble(),
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
                    upvoteColor = settings.upVoteColor?.toLong(),
                    downvoteColor = settings.downVoteColor?.toLong(),
                    hideNavigationBarWhileScrolling = if (settings.hideNavigationBarWhileScrolling) 1L else 0L,
                    zombieModeInterval = settings.zombieModeInterval.inWholeMilliseconds,
                    zombieModeScrollAmount = settings.zombieModeScrollAmount.toDouble(),
                    markAsReadWhileScrolling = if (settings.markAsReadWhileScrolling) 1L else 0L,
                    commentBarTheme = settings.commentBarTheme.toLong(),
                    replyColor = settings.replyColor?.toLong(),
                    saveColor = settings.saveColor?.toLong(),
                    searchPostTitleOnly = if (settings.searchPostTitleOnly) 1L else 0L,
                    contentFontFamily = settings.contentFontFamily.toLong(),
                    edgeToEdge = if (settings.edgeToEdge) 1L else 0L,
                    postBodyMaxLines = settings.postBodyMaxLines?.toLong(),
                    infiniteScrollEnabled = if (settings.infiniteScrollEnabled) 1L else 0L,
                    actionsOnSwipeToStartPosts = settings.actionsOnSwipeToStartPosts.map { it.toInt() }
                        .joinToString(","),
                    actionsOnSwipeToEndPosts = settings.actionsOnSwipeToEndPosts.map { it.toInt() }
                        .joinToString(","),
                    actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments.map { it.toInt() }
                        .joinToString(","),
                    actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments.map { it.toInt() }
                        .joinToString(","),
                    actionsOnSwipeToStartInbox = settings.actionsOnSwipeToStartInbox.map { it.toInt() }
                        .joinToString(","),
                    actionsOnSwipeToEndInbox = settings.actionsOnSwipeToEndInbox.map { it.toInt() }
                        .joinToString(","),
                    opaqueSystemBars = if (settings.opaqueSystemBars) 1L else 0L,
                    showScores = if (settings.showScores) 1L else 0L,
                    preferUserNicknames = if (settings.preferUserNicknames) 1L else 0L,
                    commentBarThickness = settings.commentBarThickness.toLong(),
                    imageSourcePath = if (settings.imageSourcePath) 1L else 0L,
                    defaultExploreType = settings.defaultExploreType.toLong(),
                    defaultLanguageId = settings.defaultLanguageId?.toLong(),
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
    contentFontScale = ContentFontScales(
        title = titleFontScale.toFloat(),
        body = contentFontScale.toFloat(),
        comment = commentFontScale.toFloat(),
        ancillary = ancillaryFontScale.toFloat(),
    ),
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
    upVoteColor = upvoteColor?.toInt(),
    downVoteColor = downvoteColor?.toInt(),
    hideNavigationBarWhileScrolling = hideNavigationBarWhileScrolling != 0L,
    zombieModeInterval = zombieModeInterval.milliseconds,
    zombieModeScrollAmount = zombieModeScrollAmount.toFloat(),
    markAsReadWhileScrolling = markAsReadWhileScrolling != 0L,
    commentBarTheme = commentBarTheme.toInt(),
    replyColor = replyColor?.toInt(),
    saveColor = saveColor?.toInt(),
    searchPostTitleOnly = searchPostTitleOnly != 0L,
    contentFontFamily = contentFontFamily.toInt(),
    edgeToEdge = edgeToEdge != 0L,
    postBodyMaxLines = postBodyMaxLines?.toInt(),
    infiniteScrollEnabled = infiniteScrollEnabled != 0L,
    actionsOnSwipeToStartPosts = actionsOnSwipeToStartPosts?.split(",")
        ?.mapNotNull { it.toIntOrNull()?.toActionOnSwipe() }
        ?: ActionOnSwipe.DEFAULT_SWIPE_TO_START_POSTS,
    actionsOnSwipeToEndPosts = actionsOnSwipeToEndPosts?.split(",")
        ?.mapNotNull { it.toIntOrNull()?.toActionOnSwipe() }
        ?: ActionOnSwipe.DEFAULT_SWIPE_TO_END_POSTS,
    actionsOnSwipeToStartComments = actionsOnSwipeToStartComments?.split(",")
        ?.mapNotNull { it.toIntOrNull()?.toActionOnSwipe() }
        ?: ActionOnSwipe.DEFAULT_SWIPE_TO_START_COMMENTS,
    actionsOnSwipeToEndComments = actionsOnSwipeToEndComments?.split(",")
        ?.mapNotNull { it.toIntOrNull()?.toActionOnSwipe() }
        ?: ActionOnSwipe.DEFAULT_SWIPE_TO_END_COMMENTS,
    actionsOnSwipeToStartInbox = actionsOnSwipeToStartInbox?.split(",")
        ?.mapNotNull { it.toIntOrNull()?.toActionOnSwipe() }
        ?: ActionOnSwipe.DEFAULT_SWIPE_TO_START_INBOX,
    actionsOnSwipeToEndInbox = actionsOnSwipeToEndInbox?.split(",")
        ?.mapNotNull { it.toIntOrNull()?.toActionOnSwipe() }
        ?: ActionOnSwipe.DEFAULT_SWIPE_TO_END_INBOX,
    opaqueSystemBars = opaqueSystemBars == 1L,
    showScores = showScores == 1L,
    preferUserNicknames = preferUserNicknames == 1L,
    commentBarThickness = commentBarThickness.toInt(),
    imageSourcePath = imageSourcePath == 1L,
    defaultExploreType = defaultExploreType.toInt(),
    defaultLanguageId = defaultLanguageId?.toInt(),
)
