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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.handleUrl
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.LanguageBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.UrlOpeningModeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLanguageFlag
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLanguageName
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.UrlOpeningMode
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.getCustomTabsHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.advanced.AdvancedSettingsScreen
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

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<SettingsMviModel>()
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
        val customTabsHelper = remember { getCustomTabsHelper() }

        LaunchedEffect(Unit) {
            navigationCoordinator.onDoubleTabSelection.onEach { section ->
                runCatching {
                    if (section == TabNavigationSection.Settings) {
                        scrollState.scrollTo(0)
                        topAppBarState.heightOffset = 0f
                        topAppBarState.contentOffset = 0f
                    }
                }
            }.launchIn(this)
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.subscribe(NotificationCenterEvent.CloseDialog::class).onEach {
                infoDialogOpened = false
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = {
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
                            style = MaterialTheme.typography.titleMedium,
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

                    // colors and fonts
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsColorsAndFonts,
                        disclosureIndicator = true,
                        onTap = rememberCallback {
                            navigationCoordinator.pushScreen(SettingsColorAndFontScreen())
                        },
                    )

                    // content view configuration
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsConfigureContent,
                        disclosureIndicator = true,
                        onTap = rememberCallback {
                            navigationCoordinator.pushScreen(ConfigureContentViewScreen())
                        },
                    )

                    SettingsHeader(
                        icon = Icons.Default.SettingsApplications,
                        title = LocalXmlStrings.current.settingsSectionGeneral,
                    )

                    // default listing type
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsDefaultListingType,
                        value = uiState.defaultListingType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = ListingTypeBottomSheet(
                                isLogged = uiState.isLogged,
                                screenKey = "settings",
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
                                values = uiState.availableSortTypesForPosts.map { it.toInt() },
                                expandTop = true,
                                screenKey = "settings",
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
                                comments = true,
                                values = uiState.availableSortTypesForComments.map { it.toInt() },
                                screenKey = "settings",
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
                                    SettingsMviModel.Intent.ChangeEnableSwipeActions(value),
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
                    }

                    // URL open
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsOpenUrlExternal,
                        value = uiState.urlOpeningMode.toReadableName(),
                        onTap = rememberCallback {
                            val screen = UrlOpeningModeBottomSheet(
                                values = buildList {
                                    this += UrlOpeningMode.Internal
                                    if (uiState.customTabsEnabled) {
                                        this += UrlOpeningMode.CustomTabs
                                    }
                                    this += UrlOpeningMode.External
                                },
                            )
                            navigationCoordinator.showBottomSheet(screen)
                        },
                    )

                    // advanced settings
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsAdvanced,
                        disclosureIndicator = true,
                        onTap = rememberCallback {
                            val screen = AdvancedSettingsScreen()
                            navigationCoordinator.pushScreen(screen)
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
                        },
                    )
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
                                openingMode = uiState.urlOpeningMode,
                                uriHandler = uriHandler,
                                customTabsHelper = customTabsHelper,
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
