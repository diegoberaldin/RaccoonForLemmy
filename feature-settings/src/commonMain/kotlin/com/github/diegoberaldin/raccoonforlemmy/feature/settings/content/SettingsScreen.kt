package com.github.diegoberaldin.raccoonforlemmy.feature.settings.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.toLanguageName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.FontScaleBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.LanguageBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.di.getSettingsScreenModel
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.SettingsTab
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.di.staticString
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.desc

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getSettingsScreenModel() }
        model.bindToLifecycle(SettingsTab.key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                val languageRepository = remember { getLanguageRepository() }
                val lang by languageRepository.currentLanguage.collectAsState()
                val title by remember(lang) {
                    mutableStateOf(staticString(MR.strings.navigation_settings.desc()))
                }
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                )
            },
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = Spacing.m)
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    // theme
                    SettingsRow(
                        title = stringResource(MR.strings.settings_ui_theme),
                        value = uiState.currentTheme.toReadableName(),
                        onTap = {
                            bottomSheetNavigator.show(
                                ThemeBottomSheet(
                                    onSelected = { newValue ->
                                        model.reduce(
                                            SettingsScreenMviModel.Intent.ChangeTheme(
                                                newValue,
                                            ),
                                        )
                                    },
                                    onHide = {
                                        bottomSheetNavigator.hide()
                                    },
                                ),
                            )
                        },
                    )

                    // font scale
                    SettingsRow(
                        title = stringResource(MR.strings.settings_content_font_scale),
                        value = uiState.currentFontScale.toReadableName(),
                        onTap = {
                            bottomSheetNavigator.show(
                                FontScaleBottomSheet(
                                    onSelected = { scale ->
                                        model.reduce(
                                            SettingsScreenMviModel.Intent.ChangeContentFontSize(
                                                scale,
                                            ),
                                        )
                                    },
                                    onHide = {
                                        bottomSheetNavigator.hide()
                                    },
                                ),
                            )
                        },
                    )

                    // language
                    SettingsRow(
                        title = stringResource(MR.strings.settings_language),
                        value = uiState.lang.toLanguageName(),
                        onTap = {
                            bottomSheetNavigator.show(
                                LanguageBottomSheet(
                                    onSelected = { newValue ->
                                        model.reduce(
                                            SettingsScreenMviModel.Intent.ChangeLanguage(
                                                newValue,
                                            ),
                                        )
                                    },
                                    onHide = {
                                        bottomSheetNavigator.hide()
                                    },
                                ),
                            )
                        },
                    )

                    // default listing type
                    SettingsRow(
                        title = stringResource(MR.strings.settings_default_listing_type),
                        value = uiState.defaultListingType.toReadableName(),
                        onTap = {
                            bottomSheetNavigator.show(
                                ListingTypeBottomSheet(
                                    isLogged = uiState.isLogged,
                                    onSelected = { newValue ->
                                        model.reduce(
                                            SettingsScreenMviModel.Intent.ChangeDefaultListingType(
                                                newValue,
                                            ),
                                        )
                                    },
                                    onHide = {
                                        bottomSheetNavigator.hide()
                                    },
                                ),
                            )
                        },
                    )

                    // default post sort type
                    SettingsRow(
                        title = stringResource(MR.strings.settings_default_post_sort_type),
                        value = uiState.defaultPostSortType.toReadableName(),
                        onTap = {
                            bottomSheetNavigator.show(
                                SortBottomSheet(
                                    expandTop = true,
                                    onSelected = { newValue ->
                                        model.reduce(
                                            SettingsScreenMviModel.Intent.ChangeDefaultPostSortType(
                                                newValue,
                                            ),
                                        )
                                    },
                                    onHide = {
                                        bottomSheetNavigator.hide()
                                    },
                                ),
                            )
                        },
                    )

                    // default comment sort type
                    SettingsRow(
                        title = stringResource(MR.strings.settings_default_comment_sort_type),
                        value = uiState.defaultCommentSortType.toReadableName(),
                        onTap = {
                            bottomSheetNavigator.show(
                                SortBottomSheet(
                                    values = listOf(
                                        SortType.Hot,
                                        SortType.Top.Generic,
                                        SortType.New,
                                        SortType.Old,
                                    ),
                                    onSelected = { newValue ->
                                        model.reduce(
                                            SettingsScreenMviModel.Intent.ChangeDefaultCommentSortType(
                                                newValue,
                                            ),
                                        )
                                    },
                                    onHide = {
                                        bottomSheetNavigator.hide()
                                    },
                                ),
                            )
                        },
                    )

                    // navigation bar titles
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_navigation_bar_titles_visible),
                        value = uiState.navBarTitlesVisible,
                        onValueChanged = { value ->
                            model.reduce(
                                SettingsScreenMviModel.Intent.ChangeNavBarTitlesVisible(
                                    value
                                )
                            )
                        }
                    )

                    // dynamic colors
                    if (uiState.supportsDynamicColors) {
                        SettingsSwitchRow(
                            title = stringResource(MR.strings.settings_dynamic_colors),
                            value = uiState.dynamicColors,
                            onValueChanged = { value ->
                                model.reduce(
                                    SettingsScreenMviModel.Intent.ChangeDynamicColors(
                                        value
                                    )
                                )
                            }
                        )
                    }

                    // NSFW options
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_include_nsfw),
                        value = uiState.includeNsfw,
                        onValueChanged = { value ->
                            model.reduce(SettingsScreenMviModel.Intent.ChangeIncludeNsfw(value))
                        }
                    )
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_blur_nsfw),
                        value = uiState.blurNsfw,
                        onValueChanged = { value ->
                            model.reduce(SettingsScreenMviModel.Intent.ChangeBlurNsfw(value))
                        }
                    )

                    // app version
                    SettingsRow(
                        modifier = Modifier.padding(top = Spacing.xl),
                        title = stringResource(MR.strings.settings_app_version),
                        value = uiState.appVersion,
                    )
                }
            }
        }
    }
}
