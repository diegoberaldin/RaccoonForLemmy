package com.github.diegoberaldin.raccoonforlemmy.feature.settings.main

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explicit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.handleUrl
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.BarThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.DurationBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.InboxTypeSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.LanguageBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SliderBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.getPrettyDuration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLanguageFlag
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLanguageName
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.colors.SettingsColorAndFontScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.about.AboutDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.AccountSettingsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview.ConfigureContentViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions.ConfigureSwipeActionsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.manageban.ManageBanScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<SettingsMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val notificationCenter = remember { getNotificationCenter() }
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scrollState = rememberScrollState()
        var infoDialogOpened by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val uriHandler = LocalUriHandler.current

        LaunchedEffect(Unit) {
            navigationCoordinator.onDoubleTabSelection.onEach { section ->
                if (section == TabNavigationSection.Settings) {
                    scrollState.scrollTo(0)
                    topAppBarState.heightOffset = 0f
                    topAppBarState.contentOffset = 0f
                }
            }.launchIn(this)
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.subscribe(NotificationCenterEvent.CloseDialog::class).onEach {
                infoDialogOpened = false
            }.launchIn(this)
        }

        var screenWidth by remember { mutableStateOf(0f) }
        Scaffold(
            modifier = Modifier.onGloballyPositioned {
                screenWidth = it.size.toSize().width
            }.padding(Spacing.xxs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    scope.launch {
                                        drawerCoordinator.toggleDrawer()
                                    }
                                },
                            ),
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.navigationSettings,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                )
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
                        icon = Icons.Default.Style,
                        title = LocalXmlStrings.current.settingsSectionAppearance,
                    )

                    // language
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsLanguage,
                        annotatedValue = buildAnnotatedString {
                            with(uiState.lang) {
                                append(toLanguageFlag())
                                append("  ")
                                append(toLanguageName())
                            }
                        },
                        onTap = rememberCallback {
                            val sheet = LanguageBottomSheet()
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // theme
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsUiTheme,
                        value = uiState.uiTheme.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = ThemeBottomSheet()
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // navigation bar titles
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsNavigationBarTitlesVisible,
                        value = uiState.navBarTitlesVisible,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeNavBarTitlesVisible(value)
                            )
                        },
                    )

                    // edge to edge
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsEdgeToEdge,
                        value = uiState.edgeToEdge,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeEdgeToEdge(value)
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
                                SettingsMviModel.Intent.ChangeHideNavigationBarWhileScrolling(value)
                            )
                        },
                    )

                    // colors and fonts
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsColorsAndFonts,
                        disclosureIndicator = true,
                        onTap = rememberCallback {
                            navigationCoordinator.pushScreen(SettingsColorAndFontScreen())
                        }
                    )

                    SettingsHeader(
                        icon = Icons.Default.Dashboard,
                        title = LocalXmlStrings.current.settingsSectionFeed,
                    )


                    // colors and fonts
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsConfigureContent,
                        disclosureIndicator = true,
                        onTap = rememberCallback {
                            navigationCoordinator.pushScreen(ConfigureContentViewScreen())
                        }
                    )

                    // default listing type
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsDefaultListingType,
                        value = uiState.defaultListingType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = ListingTypeBottomSheet(
                                sheetKey = key,
                                isLogged = uiState.isLogged,
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // default post sort type
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsDefaultPostSortType,
                        value = uiState.defaultPostSortType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = SortBottomSheet(
                                sheetKey = key,
                                values = uiState.availableSortTypesForPosts.map { it.toInt() },
                                expandTop = true,
                                comments = false,
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // default comment sort type
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsDefaultCommentSortType,
                        value = uiState.defaultCommentSortType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = SortBottomSheet(
                                sheetKey = key,
                                comments = true,
                                values = uiState.availableSortTypesForComments.map { it.toInt() },
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

                    SettingsHeader(
                        icon = Icons.Default.SettingsApplications,
                        title = LocalXmlStrings.current.settingsSectionBehaviour,
                    )

                    // auto-expand comments
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsAutoExpandComments,
                        value = uiState.autoExpandComments,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeAutoExpandComments(value)
                            )
                        },
                    )

                    // infinite scrolling
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsInfiniteScrollDisabled,
                        value = uiState.infiniteScrollDisabled,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeInfiniteScrollDisabled(value)
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
                                    SettingsMviModel.Intent.ChangeMarkAsReadWhileScrolling(value)
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
                            val sheet = DurationBottomSheet()
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

                    if (uiState.isLogged) {
                        // swipe actions
                        SettingsSwitchRow(
                            title = LocalXmlStrings.current.settingsEnableSwipeActions,
                            value = uiState.enableSwipeActions,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    SettingsMviModel.Intent.ChangeEnableSwipeActions(value)
                                )
                            },
                        )
                        SettingsRow(
                            title = LocalXmlStrings.current.settingsConfigureSwipeActions,
                            disclosureIndicator = true,
                            onTap = rememberCallback {
                                val screen = ConfigureSwipeActionsScreen()
                                navigationCoordinator.pushScreen(screen)
                            },
                        )

                        // double tap
                        SettingsSwitchRow(
                            title = LocalXmlStrings.current.settingsEnableDoubleTap,
                            value = uiState.enableDoubleTapAction,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    SettingsMviModel.Intent.ChangeEnableDoubleTapAction(value)
                                )
                            },
                        )
                    }

                    // URL open
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsOpenUrlExternal,
                        value = uiState.openUrlsInExternalBrowser,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeOpenUrlsInExternalBrowser(value)
                            )
                        },
                    )

                    // image loading
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsAutoLoadImages,
                        value = uiState.autoLoadImages,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeAutoLoadImages(value)
                            )
                        },
                    )

                    // search posts only in title
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsSearchPostsTitleOnly,
                        value = uiState.searchPostTitleOnly,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeSearchPostTitleOnly(value)
                            )
                        },
                    )

                    if (uiState.isLogged) {
                        SettingsHeader(
                            icon = Icons.Default.AdminPanelSettings,
                            title = LocalXmlStrings.current.settingsSectionAccount,
                        )

                        // web preferences
                        SettingsRow(
                            title = LocalXmlStrings.current.settingsWebPreferences,
                            disclosureIndicator = true,
                            onTap = rememberCallback {
                                val screen = AccountSettingsScreen()
                                navigationCoordinator.pushScreen(screen)
                            },
                        )

                        // bans and filters
                        SettingsRow(
                            title = LocalXmlStrings.current.settingsManageBan,
                            disclosureIndicator = true,
                            onTap = rememberCallback {
                                val screen = ManageBanScreen()
                                navigationCoordinator.pushScreen(screen)
                            },
                        )
                    }

                    SettingsHeader(
                        icon = Icons.Default.Explicit,
                        title = LocalXmlStrings.current.settingsSectionNsfw,
                    )

                    // NSFW options
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsIncludeNsfw,
                        value = uiState.includeNsfw,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(SettingsMviModel.Intent.ChangeIncludeNsfw(value))
                        })
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsBlurNsfw,
                        value = uiState.blurNsfw,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(SettingsMviModel.Intent.ChangeBlurNsfw(value))
                        },
                    )

                    SettingsHeader(
                        icon = Icons.Default.BugReport,
                        title = LocalXmlStrings.current.settingsSectionDebug,
                    )

                    // enable crash report
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsEnableCrashReport,
                        value = uiState.crashReportEnabled,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(SettingsMviModel.Intent.ChangeCrashReportEnabled(value))
                        },
                    )

                    // about
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsAbout,
                        value = "",
                        disclosureIndicator = true,
                        onTap = rememberCallback {
                            infoDialogOpened = true
                        },
                    )

                    // user manual
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsUserManual,
                        value = "",
                        disclosureIndicator = true,
                        onTap = rememberCallback {
                            navigationCoordinator.handleUrl(
                                url = "https://diegoberaldin.github.io/RaccoonForLemmy/user_manual/main",
                                openExternal = uiState.openUrlsInExternalBrowser,
                                uriHandler = uriHandler,
                                onOpenWeb = { url ->
                                    navigationCoordinator.pushScreen(WebViewScreen(url))
                                },
                            )
                        },
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxxl))
                }
            }
        }

        if (infoDialogOpened) {
            AboutDialog().Content()
        }
    }
}
