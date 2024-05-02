package com.github.diegoberaldin.raccoonforlemmy.feature.settings.colors

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toFontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.components.SettingsColorRow
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.components.SettingsMultiColorRow
import com.github.diegoberaldin.raccoonforlemmy.unit.choosecolor.ColorBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.unit.choosecolor.CommentBarThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.unit.choosecolor.VoteThemeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.unit.choosefont.FontFamilyBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.unit.choosefont.FontScaleBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsColorAndFontScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<SettingsColorAndFontMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val themeRepository = remember { getThemeRepository() }
        val scrollState = rememberScrollState()
        val colorSchemeProvider = remember { getColorSchemeProvider() }
        val defaultTheme = if (isSystemInDarkTheme()) {
            UiTheme.Dark
        } else {
            UiTheme.Light
        }
        var uiFontSizeWorkaround by remember { mutableStateOf(true) }

        LaunchedEffect(themeRepository) {
            themeRepository.uiFontScale.drop(1).onEach {
                uiFontSizeWorkaround = false
                delay(50)
                uiFontSizeWorkaround = true
            }.launchIn(this)
        }

        if (!uiFontSizeWorkaround) {
            return
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.settingsColorsAndFonts,
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
                    // dynamic colors
                    if (uiState.supportsDynamicColors) {
                        SettingsSwitchRow(
                            title = LocalXmlStrings.current.settingsDynamicColors,
                            value = uiState.dynamicColors,
                            onValueChanged = rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    SettingsColorAndFontMviModel.Intent.ChangeDynamicColors(value)
                                )
                            },
                        )
                    }

                    // custom scheme seed color
                    SettingsColorRow(
                        title = LocalXmlStrings.current.settingsCustomSeedColor,
                        value = uiState.customSeedColor ?: colorSchemeProvider.getColorScheme(
                            theme = uiState.uiTheme ?: defaultTheme,
                            dynamic = uiState.dynamicColors,
                        ).primary,
                        onTap = rememberCallback {
                            val sheet = ColorBottomSheet()
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    if (uiState.isLogged) {
                        // action colors
                        SettingsColorRow(
                            title = LocalXmlStrings.current.settingsUpvoteColor,
                            value = uiState.upVoteColor ?: MaterialTheme.colorScheme.primary,
                            onTap = rememberCallback {
                                val screen = VoteThemeBottomSheet(
                                    actionType = 0,
                                )
                                navigationCoordinator.showBottomSheet(screen)
                            },
                        )
                        SettingsColorRow(
                            title = LocalXmlStrings.current.settingsDownvoteColor,
                            value = uiState.downVoteColor ?: MaterialTheme.colorScheme.tertiary,
                            onTap = rememberCallback {
                                val screen = VoteThemeBottomSheet(
                                    actionType = 1,
                                )
                                navigationCoordinator.showBottomSheet(screen)
                            },
                        )
                        SettingsColorRow(
                            title = LocalXmlStrings.current.settingsReplyColor,
                            value = uiState.replyColor ?: MaterialTheme.colorScheme.secondary,
                            onTap = rememberCallback {
                                val screen = VoteThemeBottomSheet(
                                    actionType = 2,
                                )
                                navigationCoordinator.showBottomSheet(screen)
                            },
                        )
                        SettingsColorRow(
                            title = LocalXmlStrings.current.settingsSaveColor,
                            value = uiState.saveColor
                                ?: MaterialTheme.colorScheme.secondaryContainer,
                            onTap = rememberCallback {
                                val screen = VoteThemeBottomSheet(
                                    actionType = 3,
                                )
                                navigationCoordinator.showBottomSheet(screen)
                            },
                        )
                    }

                    // comment bar theme
                    val commentBarColors =
                        themeRepository.getCommentBarColors(uiState.commentBarTheme)
                    SettingsMultiColorRow(
                        title = LocalXmlStrings.current.settingsCommentBarTheme,
                        values = commentBarColors,
                        onTap = rememberCallback {
                            val screen = CommentBarThemeBottomSheet()
                            navigationCoordinator.showBottomSheet(screen)
                        }
                    )

                    // font family
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsUiFontFamily,
                        value = uiState.uiFontFamily.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = FontFamilyBottomSheet()
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )


                    // font scale
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsUiFontScale,
                        value = uiState.uiFontScale.toFontScale().toReadableName(),
                        onTap = rememberCallback {
                            val sheet = FontScaleBottomSheet()
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )
                }
            }
        }
    }
}
