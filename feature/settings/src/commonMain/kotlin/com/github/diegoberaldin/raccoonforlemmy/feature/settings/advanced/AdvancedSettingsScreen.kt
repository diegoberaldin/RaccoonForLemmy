package com.github.diegoberaldin.raccoonforlemmy.feature.settings.advanced

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.AppIconBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.BarThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.DurationBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.DurationBottomSheetType
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.InboxTypeSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SelectLanguageDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SliderBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.getPrettyDuration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.fs.getFileSystemManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private const val SETTINGS_MIME_TYPE = "application/json"
private const val SETTINGS_FILE_NAME = "raccoon_settings.json"

class AdvancedSettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<AdvancedSettingsMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val scrollState = rememberScrollState()
        var screenWidth by remember { mutableStateOf(0f) }
        var languageDialogOpened by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        val successMessage = LocalXmlStrings.current.messageOperationSuccessful
        val errorMessage = LocalXmlStrings.current.messageGenericError
        val scope = rememberCoroutineScope()
        val fileSystemManager = remember { getFileSystemManager() }
        var fileInputOpened by remember { mutableStateOf(false) }
        var settingsContent by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(model) {
            model.effects.onEach { evt ->
                when (evt) {
                    is AdvancedSettingsMviModel.Effect.SaveSettings -> {
                        settingsContent = evt.content
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .onGloballyPositioned {
                    screenWidth = it.size.toSize().width
                }.padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.settingsAdvanced,
                        )
                    },
                    navigationIcon = {
                        if (navigationCoordinator.canPop.value) {
                            Image(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        navigationCoordinator.popScreen()
                                    },
                                ),
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                        }
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        snackbarData = data,
                    )
                }
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    SettingsHeader(
                        title = LocalXmlStrings.current.settingsTitleDisplay,
                        icon = Icons.Default.DisplaySettings,
                    )
                    // navigation bar titles
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsNavigationBarTitlesVisible,
                        value = uiState.navBarTitlesVisible,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                AdvancedSettingsMviModel.Intent.ChangeNavBarTitlesVisible(value),
                            )
                        },
                    )

                    // edge to edge
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsEdgeToEdge,
                        value = uiState.edgeToEdge,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                AdvancedSettingsMviModel.Intent.ChangeEdgeToEdge(value)
                            )
                        },
                    )

                    // system bar theme
                    if (uiState.edgeToEdge) {
                        val barThemeName = if (uiState.opaqueSystemBars) {
                            UiBarTheme.Opaque.toReadableName()
                        } else {
                            UiBarTheme.Transparent.toReadableName()
                        }
                        SettingsRow(
                            title = LocalXmlStrings.current.settingsBarTheme,
                            value = barThemeName,
                            onTap = rememberCallback {
                                val sheet = BarThemeBottomSheet()
                                navigationCoordinator.showBottomSheet(sheet)
                            },
                        )
                    }

                    // bottom navigation hiding
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsHideNavigationBar,
                        value = uiState.hideNavigationBarWhileScrolling,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                AdvancedSettingsMviModel.Intent.ChangeHideNavigationBarWhileScrolling(
                                    value
                                )
                            )
                        },
                    )

                    SettingsHeader(
                        title = LocalXmlStrings.current.settingsTitleReading,
                        icon = Icons.AutoMirrored.Default.Article,
                    )
                    if (uiState.isLogged) {
                        // visually differentiate read posts
                        SettingsSwitchRow(
                            title = LocalXmlStrings.current.settingsFadeReadPosts,
                            value = uiState.fadeReadPosts,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    AdvancedSettingsMviModel.Intent.ChangeFadeReadPosts(value)
                                )
                            },
                        )

                        // show unread comment number
                        SettingsSwitchRow(
                            title = LocalXmlStrings.current.settingsShowUnreadComments,
                            value = uiState.showUnreadComments,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    AdvancedSettingsMviModel.Intent.ChangeShowUnreadComments(value)
                                )
                            },
                        )
                    }

                    // default explore type
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsDefaultExploreType,
                        value = uiState.defaultExploreType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = ListingTypeBottomSheet(
                                isLogged = uiState.isLogged,
                                screenKey = "advancedSettings",
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )
                    if (uiState.isLogged) {
                        // default inbox type
                        SettingsRow(
                            title = LocalXmlStrings.current.settingsDefaultInboxType,
                            value = if (uiState.defaultInboxUnreadOnly) {
                                LocalXmlStrings.current.inboxListingTypeUnread
                            } else {
                                LocalXmlStrings.current.inboxListingTypeAll
                            },
                            onTap = rememberCallback {
                                val sheet = InboxTypeSheet()
                                navigationCoordinator.showBottomSheet(sheet)
                            },
                        )
                    }

                    // default language
                    val languageValue = uiState.availableLanguages.firstOrNull { l ->
                        l.id == uiState.defaultLanguageId
                    }?.takeIf { l ->
                        l.id > 0 // undetermined language
                    }?.name ?: LocalXmlStrings.current.undetermined
                    SettingsRow(
                        title = LocalXmlStrings.current.advancedSettingsDefaultLanguage,
                        value = languageValue,
                        onTap = rememberCallback {
                            languageDialogOpened = true
                        },
                    )

                    // infinite scrolling
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsInfiniteScrollDisabled,
                        value = uiState.infiniteScrollDisabled,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                AdvancedSettingsMviModel.Intent.ChangeInfiniteScrollDisabled(value)
                            )
                        },
                    )

                    // auto-expand comments
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsAutoExpandComments,
                        value = uiState.autoExpandComments,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                AdvancedSettingsMviModel.Intent.ChangeAutoExpandComments(value)
                            )
                        },
                    )

                    if (uiState.isLogged) {
                        // mark as read while scrolling
                        SettingsSwitchRow(
                            title = LocalXmlStrings.current.settingsMarkAsReadWhileScrolling,
                            value = uiState.markAsReadWhileScrolling,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    AdvancedSettingsMviModel.Intent.ChangeMarkAsReadWhileScrolling(
                                        value
                                    )
                                )
                            },
                        )
                    }

                    // zombie mode interval
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsZombieModeInterval,
                        value = uiState.zombieModeInterval.getPrettyDuration(
                            secondsLabel = LocalXmlStrings.current.postSecondShort,
                            minutesLabel = LocalXmlStrings.current.postMinuteShort,
                            hoursLabel = LocalXmlStrings.current.homeSortTypeTop6Hours,
                        ),
                        onTap = rememberCallback {
                            val sheet = DurationBottomSheet(
                                type = DurationBottomSheetType.ZOMBIE_MODE_INTERVAL,
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // zombie scroll amount
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsZombieModeScrollAmount,
                        value = buildString {
                            val pt = uiState.zombieModeScrollAmount.toLocalDp().value.roundToInt()
                            append(pt)
                            append(LocalXmlStrings.current.settingsPointsShort)
                        },
                        onTap = rememberCallback {
                            val sheet = SliderBottomSheet(
                                min = 0f,
                                max = screenWidth,
                                initial = uiState.zombieModeScrollAmount,
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // enable buttons to scroll between comments
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsEnableButtonsToScrollBetweenComments,
                        value = uiState.enableButtonsToScrollBetweenComments,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                AdvancedSettingsMviModel.Intent.ChangeEnableButtonsToScrollBetweenComments(value)
                            )
                        },
                    )

                    SettingsHeader(
                        title = LocalXmlStrings.current.settingsTitlePictures,
                        icon = Icons.Default.Photo,
                    )
                    // image loading
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsAutoLoadImages,
                        value = uiState.autoLoadImages,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                AdvancedSettingsMviModel.Intent.ChangeAutoLoadImages(value)
                            )
                        },
                    )

                    if (uiState.imageSourceSupported) {
                        // image source path
                        SettingsSwitchRow(
                            title = LocalXmlStrings.current.settingsItemImageSourcePath,
                            subtitle = LocalXmlStrings.current.settingsSubtitleImageSourcePath,
                            value = uiState.imageSourcePath,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    AdvancedSettingsMviModel.Intent.ChangeImageSourcePath(value),
                                )
                            },
                        )
                    }

                    SettingsHeader(
                        title = LocalXmlStrings.current.settingsTitleExperimental,
                        icon = Icons.Default.Science,
                    )
                    if (uiState.isLogged) {
                        // double tap
                        SettingsSwitchRow(
                            title = LocalXmlStrings.current.settingsEnableDoubleTap,
                            value = uiState.enableDoubleTapAction,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    AdvancedSettingsMviModel.Intent.ChangeEnableDoubleTapAction(
                                        value
                                    )
                                )
                            },
                        )

                    }
                    // search posts only in title
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsSearchPostsTitleOnly,
                        value = uiState.searchPostTitleOnly,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                AdvancedSettingsMviModel.Intent.ChangeSearchPostTitleOnly(value)
                            )
                        },
                    )

                    if (uiState.isLogged) {
                        // check inbox unread items
                        SettingsRow(
                            title = buildString {
                                append(LocalXmlStrings.current.settingsInboxBackgroundCheckPeriod)
                            },
                            value = uiState.inboxBackgroundCheckPeriod.let { value ->
                                value?.getPrettyDuration(
                                    secondsLabel = LocalXmlStrings.current.postSecondShort,
                                    minutesLabel = LocalXmlStrings.current.postMinuteShort,
                                    hoursLabel = LocalXmlStrings.current.postHourShort,
                                ) ?: LocalXmlStrings.current.never
                            },
                            onTap = {
                                val sheet = DurationBottomSheet(
                                    values = listOf(
                                        15.minutes,
                                        30.minutes,
                                        1.hours,
                                        2.hours,
                                        5.hours,
                                    ),
                                    type = DurationBottomSheetType.INBOX_CHECK_PERIOD,
                                )
                                navigationCoordinator.showBottomSheet(sheet)
                            },
                        )
                    }

                    // custom app icon
                    if (uiState.appIconChangeSupported) {
                        SettingsRow(
                            title = buildString {
                                append(LocalXmlStrings.current.settingsAppIcon)
                                append(" ")
                                append(LocalXmlStrings.current.requiresRestart)
                            },
                            onTap = rememberCallback {
                                val sheet = AppIconBottomSheet()
                                navigationCoordinator.showBottomSheet(sheet)
                            },
                        )
                    }

                    if (uiState.supportSettingsImportExport) {
                        SettingsRow(
                            title = LocalXmlStrings.current.settingsExport,
                            onTap = rememberCallback(model) {
                                model.reduce(AdvancedSettingsMviModel.Intent.ExportSettings)
                            }
                        )
                        SettingsRow(
                            title = LocalXmlStrings.current.settingsImport,
                            onTap = rememberCallback {
                                fileInputOpened = true
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xxxl))
                }
            }
        }

        if (uiState.loading) {
            ProgressHud()
        }

        if (languageDialogOpened) {
            SelectLanguageDialog(
                currentLanguageId = uiState.defaultLanguageId,
                languages = uiState.availableLanguages,
                onSelect = { languageId ->
                    model.reduce(AdvancedSettingsMviModel.Intent.ChangeDefaultLanguage(languageId))
                },
                onDismiss = {
                    languageDialogOpened = false
                }
            )
        }

        if (fileInputOpened) {
            fileSystemManager.readFromFile(mimeTypes = arrayOf(SETTINGS_MIME_TYPE)) { content ->
                if (content != null) {
                    model.reduce(AdvancedSettingsMviModel.Intent.ImportSettings(content))
                }
                fileInputOpened = false
            }
        }

        settingsContent?.also { content ->
            fileSystemManager.writeToFile(
                mimeType = SETTINGS_MIME_TYPE,
                name = SETTINGS_FILE_NAME,
                data = content
            ) { success ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (success) {
                            successMessage
                        } else {
                            errorMessage
                        }
                    )
                }
                settingsContent = null
            }
        }
    }
}
