package com.github.diegoberaldin.raccoonforlemmy.feature.settings.advanced

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.BarThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.DurationBottomSheet
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
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import kotlin.math.roundToInt

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
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                        }
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
                        icon = Icons.Default.Book,
                    )
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
                }
            }
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
    }
}