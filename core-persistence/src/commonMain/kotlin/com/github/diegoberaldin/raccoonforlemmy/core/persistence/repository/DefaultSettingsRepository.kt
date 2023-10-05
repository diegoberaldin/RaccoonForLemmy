package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.SettingsEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

private object KeyStoreKeys {
    const val UiTheme = "uiTheme"
    const val ContentFontScale = "contentFontSize"
    const val Locale = "locale"
    const val DefaultListingType = "defaultListingType"
    const val DefaultPostSortType = "defaultPostSortType"
    const val DefaultCommentSortType = "defaultCommentSortType"
    const val IncludeNsfw = "includeNsfw"
    const val BlurNsfw = "blurNsfw"
    const val NavItemTitlesVisible = "navItemTitlesVisible"
    const val DynamicColors = "dynamicColors"
    const val OpenUrlsInExternalBrowser = "openUrlsInExternalBrowser"
    const val EnableSwipeActions = "enableSwipeActions"
    const val CustomSeedColor = "customPrimaryColor"
    const val PostLayout = "postLayout"
}

internal class DefaultSettingsRepository(
    val provider: DatabaseProvider,
    private val keyStore: TemporaryKeyStore,
) : SettingsRepository {

    private val db = provider.getDatabase()

    override val currentSettings = MutableStateFlow(SettingsModel())

    override suspend fun createSettings(settings: SettingsModel, accountId: Long) =
        withContext(Dispatchers.IO) {
            db.settingsQueries.create(
                theme = settings.theme?.toLong(),
                contentFontScale = settings.contentFontScale.toDouble(),
                locale = settings.locale,
                defaultListingType = settings.defaultListingType.toLong(),
                defaultPostSortType = settings.defaultPostSortType.toLong(),
                defaultCommentSortType = settings.defaultCommentSortType.toLong(),
                includeNsfw = if (settings.includeNsfw) 1L else 0L,
                blurNsfw = if (settings.blurNsfw) 1L else 0L,
                navigationTitlesVisible = if (settings.navigationTitlesVisible) 1L else 0L,
                dynamicColors = if (settings.dynamicColors) 1L else 0L,
                openUrlsInExternalBrowser = if (settings.openUrlsInExternalBrowser) 1L else 0L,
                enableSwipeActions = if (settings.enableSwipeActions) 1L else 0L,
                customSeedColor = settings.customSeedColor?.toLong(),
                account_id = accountId,
                postLayout = settings.postLayout.toLong(),
            )
        }

    override suspend fun getSettings(accountId: Long?): SettingsModel =
        withContext(Dispatchers.IO) {
            if (accountId == null) {
                // anonymous user, reading from keystore
                SettingsModel(
                    theme = keyStore[KeyStoreKeys.UiTheme, 0],
                    contentFontScale = keyStore[KeyStoreKeys.ContentFontScale, 1f],
                    locale = keyStore[KeyStoreKeys.Locale, ""].takeIf { it.isNotEmpty() },
                    defaultListingType = keyStore[KeyStoreKeys.DefaultListingType, 0],
                    defaultPostSortType = keyStore[KeyStoreKeys.DefaultPostSortType, 0],
                    defaultCommentSortType = keyStore[KeyStoreKeys.DefaultCommentSortType, 0],
                    includeNsfw = keyStore[KeyStoreKeys.IncludeNsfw, true],
                    blurNsfw = keyStore[KeyStoreKeys.BlurNsfw, true],
                    navigationTitlesVisible = keyStore[KeyStoreKeys.NavItemTitlesVisible, true],
                    dynamicColors = keyStore[KeyStoreKeys.DynamicColors, false],
                    openUrlsInExternalBrowser = keyStore[KeyStoreKeys.OpenUrlsInExternalBrowser, false],
                    enableSwipeActions = keyStore[KeyStoreKeys.EnableSwipeActions, true],
                    customSeedColor = if (!keyStore.containsKey(KeyStoreKeys.CustomSeedColor)) null else keyStore[KeyStoreKeys.CustomSeedColor, 0],
                    postLayout = keyStore[KeyStoreKeys.PostLayout, 0],
                )
            } else {
                db.settingsQueries.getBy(accountId)
                    .executeAsOneOrNull()?.toModel() ?: SettingsModel()
            }
        }

    override suspend fun updateSettings(settings: SettingsModel, accountId: Long?) =
        withContext(Dispatchers.IO) {
            if (accountId == null) {
                // anonymous user, storing into keystore
                if (settings.theme != null) {
                    keyStore.save(KeyStoreKeys.UiTheme, settings.theme)
                }
                keyStore.save(KeyStoreKeys.ContentFontScale, settings.contentFontScale)
                if (!settings.locale.isNullOrEmpty()) {
                    keyStore.save(KeyStoreKeys.Locale, settings.locale)
                }
                keyStore.save(KeyStoreKeys.DefaultListingType, settings.defaultListingType)
                keyStore.save(KeyStoreKeys.DefaultPostSortType, settings.defaultPostSortType)
                keyStore.save(KeyStoreKeys.DefaultCommentSortType, settings.defaultCommentSortType)
                keyStore.save(KeyStoreKeys.IncludeNsfw, settings.includeNsfw)
                keyStore.save(KeyStoreKeys.BlurNsfw, settings.blurNsfw)
                keyStore.save(KeyStoreKeys.NavItemTitlesVisible, settings.navigationTitlesVisible)
                keyStore.save(KeyStoreKeys.DynamicColors, settings.dynamicColors)
                keyStore.save(
                    key = KeyStoreKeys.OpenUrlsInExternalBrowser,
                    value = settings.openUrlsInExternalBrowser
                )
                keyStore.save(KeyStoreKeys.EnableSwipeActions, settings.enableSwipeActions)
                if (settings.customSeedColor != null) {
                    keyStore.save(KeyStoreKeys.CustomSeedColor, settings.customSeedColor)
                }
                keyStore.save(KeyStoreKeys.PostLayout, settings.postLayout)
            } else {
                db.settingsQueries.update(
                    theme = settings.theme?.toLong(),
                    contentFontScale = settings.contentFontScale.toDouble(),
                    locale = settings.locale,
                    defaultListingType = settings.defaultListingType.toLong(),
                    defaultPostSortType = settings.defaultPostSortType.toLong(),
                    defaultCommentSortType = settings.defaultCommentSortType.toLong(),
                    includeNsfw = if (settings.includeNsfw) 1L else 0L,
                    blurNsfw = if (settings.blurNsfw) 1L else 0L,
                    navigationTitlesVisible = if (settings.navigationTitlesVisible) 1L else 0L,
                    dynamicColors = if (settings.dynamicColors) 1L else 0L,
                    openUrlsInExternalBrowser = if (settings.openUrlsInExternalBrowser) 1L else 0L,
                    enableSwipeActions = if (settings.enableSwipeActions) 1L else 0L,
                    customSeedColor = settings.customSeedColor?.toLong(),
                    postLayout = settings.postLayout.toLong(),
                    account_id = accountId,
                )
            }
        }

    override fun changeCurrentSettings(settings: SettingsModel) {
        currentSettings.value = settings
    }
}

private fun SettingsEntity.toModel() = SettingsModel(
    id = id,
    theme = theme?.toInt(),
    contentFontScale = contentFontScale.toFloat(),
    locale = locale,
    defaultListingType = defaultListingType.toInt(),
    defaultPostSortType = defaultPostSortType.toInt(),
    defaultCommentSortType = defaultCommentSortType.toInt(),
    includeNsfw = includeNsfw != 0L,
    blurNsfw = blurNsfw != 0L,
    navigationTitlesVisible = navigationTitlesVisible != 0L,
    dynamicColors = dynamicColors != 0L,
    openUrlsInExternalBrowser = openUrlsInExternalBrowser != 0L,
    enableSwipeActions = enableSwipeActions != 0L,
    customSeedColor = customSeedColor?.toInt(),
    postLayout = postLayout.toInt(),
)
