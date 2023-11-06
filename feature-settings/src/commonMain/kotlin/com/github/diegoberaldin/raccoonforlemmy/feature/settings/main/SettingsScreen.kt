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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.FontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ColorBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ColorPickerDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.DurationBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.FontFamilyBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.FontScaleBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.LanguageBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.PostLayoutBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.getPrettyDuration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLanguageName
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.di.getSettingsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.dialog.AboutDialog
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.SettingsTab
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.components.SettingsColorRow
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.components.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.components.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.components.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.di.staticString
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getSettingsViewModel() }
        model.bindToLifecycle(SettingsTab.key)
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val notificationCenter = remember { getNotificationCenter() }
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val scope = rememberCoroutineScope()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scrollState = rememberScrollState()
        val languageRepository = remember { getLanguageRepository() }
        val lang by languageRepository.currentLanguage.collectAsState()
        var uiFontSizeWorkaround by remember { mutableStateOf(true) }
        val themeRepository = remember { getThemeRepository() }
        var upvoteColorDialogOpened by remember { mutableStateOf(false) }
        var downvoteColorDialogOpened by remember { mutableStateOf(false) }
        var infoDialogOpened by remember { mutableStateOf(false) }

        LaunchedEffect(themeRepository) {
            themeRepository.uiFontScale.drop(1).onEach {
                uiFontSizeWorkaround = false
                delay(50)
                uiFontSizeWorkaround = true
            }.launchIn(this)
        }
        LaunchedEffect(Unit) {
            navigationCoordinator.onDoubleTabSelection.onEach { tab ->
                if (tab == SettingsTab) {
                    scrollState.scrollTo(0)
                    topAppBarState.heightOffset = 0f
                    topAppBarState.contentOffset = 0f
                }
            }.launchIn(this)
        }
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.addObserver(
                { result ->
                    (result as? String)?.also { lang ->
                        model.reduce(SettingsMviModel.Intent.ChangeLanguage(lang))
                    }
                }, key, NotificationCenterContractKeys.ChangeLanguage
            )
            notificationCenter.addObserver(
                { result ->
                    (result as? UiTheme)?.also { value ->
                        model.reduce(SettingsMviModel.Intent.ChangeUiTheme(value))
                    }
                }, key, NotificationCenterContractKeys.ChangeTheme
            )
            notificationCenter.addObserver(
                { result ->
                    model.reduce(
                        SettingsMviModel.Intent.ChangeCustomSeedColor(
                            result as? Color?
                        )
                    )
                }, key, NotificationCenterContractKeys.ChangeColor
            )
            notificationCenter.addObserver(
                { result ->
                    (result as? UiFontFamily)?.also { value ->
                        model.reduce(
                            SettingsMviModel.Intent.ChangeUiFontFamily(
                                value
                            )
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeFontFamily
            )
            notificationCenter.addObserver(
                { result ->
                    (result as? Float)?.also { value ->
                        model.reduce(
                            SettingsMviModel.Intent.ChangeUiFontSize(
                                value
                            )
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeFontSize
            )
            notificationCenter.addObserver(
                { result ->
                    (result as? Float)?.also { value ->
                        model.reduce(
                            SettingsMviModel.Intent.ChangeContentFontSize(
                                value
                            )
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeFontSize
            )
            notificationCenter.addObserver(
                { result ->
                    (result as? PostLayout)?.also { value ->
                        model.reduce(
                            SettingsMviModel.Intent.ChangePostLayout(
                                value
                            )
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangePostLayout
            )
            notificationCenter.addObserver(
                { result ->
                    (result as? ListingType)?.also {
                        model.reduce(
                            SettingsMviModel.Intent.ChangeDefaultListingType(
                                it
                            )
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeFeedType
            )
            notificationCenter.addObserver(
                {
                    (it as? SortType)?.also { sortType ->
                        model.reduce(
                            SettingsMviModel.Intent.ChangeDefaultPostSortType(
                                sortType
                            )
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeSortType
            )
            notificationCenter.addObserver(
                {
                    (it as? SortType)?.also { sortType ->
                        model.reduce(
                            SettingsMviModel.Intent.ChangeDefaultCommentSortType(
                                sortType
                            )
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeCommentSortType
            )
            notificationCenter.addObserver(
                {
                    infoDialogOpened = false
                },
                key, NotificationCenterContractKeys.CloseDialog,
            )
            notificationCenter.addObserver(
                {
                    (it as? Duration)?.also { value ->
                        model.reduce(
                            SettingsMviModel.Intent.ChangeZombieModeInterval(
                                value,
                            )
                        )
                    }
                },
                key, NotificationCenterContractKeys.ChangeZombieInterval
            )
        }

        if (!uiFontSizeWorkaround) {
            return
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                val title by remember(lang) {
                    mutableStateOf(staticString(MR.strings.navigation_settings.desc()))
                }
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                rememberCallback {
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
                            text = title,
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
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    SettingsHeader(
                        icon = Icons.Default.Palette,
                        title = stringResource(MR.strings.settings_section_appearance),
                    )

                    // language
                    SettingsRow(
                        title = stringResource(MR.strings.settings_language),
                        value = uiState.lang.toLanguageName(),
                        onTap = rememberCallback {
                            val sheet = LanguageBottomSheet()
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )

                    // theme
                    SettingsRow(
                        title = stringResource(MR.strings.settings_ui_theme),
                        value = uiState.uiTheme.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = ThemeBottomSheet()
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )

                    // dynamic colors
                    if (uiState.supportsDynamicColors) {
                        SettingsSwitchRow(
                            title = stringResource(MR.strings.settings_dynamic_colors),
                            value = uiState.dynamicColors,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    SettingsMviModel.Intent.ChangeDynamicColors(
                                        value
                                    )
                                )
                            }
                        )
                    }

                    val colorSchemeProvider = remember { getColorSchemeProvider() }
                    // custom scheme seed color
                    SettingsColorRow(
                        title = stringResource(MR.strings.settings_custom_seed_color),
                        value = uiState.customSeedColor ?: colorSchemeProvider.getColorScheme(
                            theme = uiState.uiTheme,
                            dynamic = uiState.dynamicColors,
                        ).primary,
                        onTap = rememberCallback {
                            val sheet = ColorBottomSheet()
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )
                    // upvote and downvote colors
                    SettingsColorRow(
                        title = stringResource(MR.strings.settings_upvote_color),
                        value = uiState.upvoteColor ?: MaterialTheme.colorScheme.primary,
                        onTap = rememberCallback {
                            upvoteColorDialogOpened = true
                        },
                    )
                    SettingsColorRow(
                        title = stringResource(MR.strings.settings_downvote_color),
                        value = uiState.downvoteColor ?: MaterialTheme.colorScheme.tertiary,
                        onTap = rememberCallback {
                            downvoteColorDialogOpened = true
                        },
                    )

                    // font family
                    SettingsRow(
                        title = stringResource(MR.strings.settings_ui_font_family),
                        value = uiState.uiFontFamily.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = FontFamilyBottomSheet()
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )
                    // font scale
                    SettingsRow(
                        title = stringResource(MR.strings.settings_ui_font_scale),
                        value = uiState.uiFontScale.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = FontScaleBottomSheet(
                                values = listOf(
                                    FontScale.Large,
                                    FontScale.Normal,
                                    FontScale.Small,
                                ),
                            )
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )
                    SettingsRow(
                        title = stringResource(MR.strings.settings_content_font_scale),
                        value = uiState.contentFontScale.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = FontScaleBottomSheet()
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )

                    // post layout
                    SettingsRow(
                        title = stringResource(MR.strings.settings_post_layout),
                        value = uiState.postLayout.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = PostLayoutBottomSheet()
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )

                    // separate upvotes and downvotes
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_separate_up_and_downvotes),
                        value = uiState.separateUpAndDownVotes,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeSeparateUpAndDownVotes(
                                    value
                                )
                            )
                        }
                    )

                    // full height images
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_full_height_images),
                        value = uiState.fullHeightImages,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeFullHeightImages(
                                    value
                                )
                            )
                        }
                    )

                    // navigation bar titles
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_navigation_bar_titles_visible),
                        value = uiState.navBarTitlesVisible,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeNavBarTitlesVisible(
                                    value
                                )
                            )
                        }
                    )

                    SettingsHeader(
                        icon = Icons.Default.Tune,
                        title = stringResource(MR.strings.settings_section_feed),
                    )

                    // default listing type
                    SettingsRow(
                        title = stringResource(MR.strings.settings_default_listing_type),
                        value = uiState.defaultListingType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = ListingTypeBottomSheet(
                                isLogged = uiState.isLogged,
                            )
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )

                    // default post sort type
                    SettingsRow(
                        title = stringResource(MR.strings.settings_default_post_sort_type),
                        value = uiState.defaultPostSortType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = SortBottomSheet(
                                expandTop = true,
                                contract = NotificationCenterContractKeys.ChangeSortType,
                            )
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )

                    // default comment sort type
                    SettingsRow(
                        title = stringResource(MR.strings.settings_default_comment_sort_type),
                        value = uiState.defaultCommentSortType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = SortBottomSheet(
                                contract = NotificationCenterContractKeys.ChangeCommentSortType,
                                values = listOf(
                                    SortType.Hot,
                                    SortType.Top.Generic,
                                    SortType.New,
                                    SortType.Old,
                                    SortType.Controversial,
                                ),
                            )
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )

                    SettingsHeader(
                        icon = Icons.Default.SettingsApplications,
                        title = stringResource(MR.strings.settings_section_behaviour),
                    )

                    // zombie mode interval
                    SettingsRow(
                        title = stringResource(MR.strings.settings_zombie_mode_interval),
                        value = uiState.zombieModeInterval.getPrettyDuration(
                            secondsLabel = stringResource(MR.strings.post_second_short),
                            minutesLabel = stringResource(MR.strings.post_minute_short),
                            hoursLabel = stringResource(MR.strings.post_hour_short),
                        ),
                        onTap = rememberCallback {
                            val sheet = DurationBottomSheet()
                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                        },
                    )

                    // swipe actions
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_enable_swipe_actions),
                        value = uiState.enableSwipeActions,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeEnableSwipeActions(
                                    value
                                )
                            )
                        }
                    )

                    // bottom navigation hiding
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_hide_navigation_bar),
                        value = uiState.hideNavigationBarWhileScrolling,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeHideNavigationBarWhileScrolling(
                                    value
                                )
                            )
                        }
                    )

                    // URL open
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_open_url_external),
                        value = uiState.openUrlsInExternalBrowser,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeOpenUrlsInExternalBrowser(
                                    value
                                )
                            )
                        }
                    )

                    // auto-expand comments
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_auto_expand_comments),
                        value = uiState.autoExpandComments,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeAutoExpandComments(
                                    value
                                )
                            )
                        }
                    )

                    // image loading
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_auto_load_images),
                        value = uiState.autoLoadImages,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                SettingsMviModel.Intent.ChangeAutoLoadImages(
                                    value
                                )
                            )
                        }
                    )

                    SettingsHeader(
                        icon = Icons.Default.Shield,
                        title = stringResource(MR.strings.settings_section_nsfw),
                    )

                    // NSFW options
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_include_nsfw),
                        value = uiState.includeNsfw,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(SettingsMviModel.Intent.ChangeIncludeNsfw(value))
                        }
                    )
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_blur_nsfw),
                        value = uiState.blurNsfw,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(SettingsMviModel.Intent.ChangeBlurNsfw(value))
                        }
                    )

                    SettingsHeader(
                        icon = Icons.Default.BugReport,
                        title = stringResource(MR.strings.settings_section_debug),
                    )

                    // enable crash report
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_enable_crash_report),
                        value = uiState.crashReportEnabled,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(SettingsMviModel.Intent.ChangeCrashReportEnabled(value))
                        }
                    )

                    // about
                    SettingsRow(
                        title = stringResource(MR.strings.settings_about),
                        value = "",
                        onTap = rememberCallback {
                            infoDialogOpened = true
                        },
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxxl))
                }
            }
        }

        if (upvoteColorDialogOpened) {
            val initial = uiState.upvoteColor ?: MaterialTheme.colorScheme.primary
            ColorPickerDialog(
                initialValue = initial,
                onClose = rememberCallback {
                    upvoteColorDialogOpened = false
                },
                onSubmit = rememberCallbackArgs { color ->
                    upvoteColorDialogOpened = false
                    model.reduce(SettingsMviModel.Intent.ChangeUpvoteColor(color))
                },
                onReset = rememberCallback(model) {
                    upvoteColorDialogOpened = false
                    val scheme = getColorSchemeProvider().getColorScheme(
                        theme = uiState.uiTheme,
                        dynamic = uiState.dynamicColors,
                        customSeed = uiState.customSeedColor
                    )
                    val defaultValue = scheme.primary
                    model.reduce(SettingsMviModel.Intent.ChangeUpvoteColor(defaultValue))
                },
            )
        }

        if (downvoteColorDialogOpened) {
            val initial = uiState.downvoteColor ?: MaterialTheme.colorScheme.tertiary
            ColorPickerDialog(
                initialValue = initial,
                onClose = rememberCallback {
                    downvoteColorDialogOpened = false
                },
                onSubmit = rememberCallbackArgs(model) { color ->
                    downvoteColorDialogOpened = false
                    model.reduce(SettingsMviModel.Intent.ChangeDownvoteColor(color))
                },
                onReset = rememberCallback(model) {
                    downvoteColorDialogOpened = false
                    model.reduce(SettingsMviModel.Intent.ChangeDownvoteColor(null))
                },
            )
        }

        if (infoDialogOpened) {
            AboutDialog().Content()
        }
    }
}
