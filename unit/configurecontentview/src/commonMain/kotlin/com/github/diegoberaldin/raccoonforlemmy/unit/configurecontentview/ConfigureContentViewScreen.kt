package com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview

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
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TextFormat
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toFontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.CommentBarThicknessBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.PostBodyMaxLinesBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.PostLayoutBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.VoteFormatBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.unit.choosefont.FontFamilyBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.unit.choosefont.FontScaleBottomSheet

class ConfigureContentViewScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ConfigureContentViewMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val scrollState = rememberScrollState()

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.settingsConfigureContent,
                        )
                    },
                    navigationIcon = {
                        if (navigationCoordinator.canPop.value) {
                            Image(
                                modifier = Modifier.onClick(
                                    onClick = {
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
                    SettingsHeader(
                        icon = Icons.Default.TextFormat,
                        title = LocalXmlStrings.current.settingsConfigureText,
                    )

                    // content font family
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsContentFontFamily,
                        value = uiState.contentFontFamily.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = FontFamilyBottomSheet(content = true)
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // content font scale
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsTitleFontScale,
                        value = uiState.contentFontScale.title.toFontScale().toReadableName(),
                        onTap = rememberCallback {
                            val sheet = FontScaleBottomSheet(contentClass = ContentFontClass.Title)
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsContentFontScale,
                        value = uiState.contentFontScale.body.toFontScale().toReadableName(),
                        onTap = rememberCallback {
                            val sheet = FontScaleBottomSheet(contentClass = ContentFontClass.Body)
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsCommentFontScale,
                        value = uiState.contentFontScale.comment.toFontScale().toReadableName(),
                        onTap = rememberCallback {
                            val sheet =
                                FontScaleBottomSheet(contentClass = ContentFontClass.Comment)
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsAncillaryFontScale,
                        value = uiState.contentFontScale.ancillary.toFontScale().toReadableName(),
                        onTap = rememberCallback {
                            val sheet =
                                FontScaleBottomSheet(contentClass = ContentFontClass.AncillaryText)
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    SettingsHeader(
                        icon = Icons.Default.Style,
                        title = LocalXmlStrings.current.settingsConfigureCustomizations,
                    )

                    // post layout
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsPostLayout,
                        value = uiState.postLayout.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = PostLayoutBottomSheet()
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // body max lines in full layout
                    if (uiState.postLayout == PostLayout.Full) {
                        SettingsRow(
                            title = LocalXmlStrings.current.settingsPostBodyMaxLines,
                            value = if (uiState.postBodyMaxLines == null) {
                                LocalXmlStrings.current.settingsPostBodyMaxLinesUnlimited
                            } else {
                                uiState.postBodyMaxLines.toString()
                            },
                            onTap = rememberCallback {
                                val screen = PostBodyMaxLinesBottomSheet()
                                navigationCoordinator.showBottomSheet(screen)
                            },
                        )
                    }

                    // prefer user nicknames
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsPreferUserNicknames,
                        value = uiState.preferUserNicknames,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                ConfigureContentViewMviModel.Intent.ChangePreferUserNicknames(value)
                            )
                        },
                    )

                    // full height images
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsFullHeightImages,
                        value = uiState.fullHeightImages,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                ConfigureContentViewMviModel.Intent.ChangeFullHeightImages(value)
                            )
                        },
                    )

                    // full width images
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsFullWidthImages,
                        value = uiState.fullWidthImages,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(
                                ConfigureContentViewMviModel.Intent.ChangeFullWidthImages(value)
                            )
                        },
                    )

                    // vote format
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsVoteFormat,
                        value = uiState.voteFormat.toReadableName(),
                        onTap = {
                            val sheet = VoteFormatBottomSheet()
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // comment bar thickness
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsCommentBarThickness,
                        value = uiState.commentBarThickness.toString(),
                        onTap = rememberCallback {
                            val screen = CommentBarThicknessBottomSheet()
                            navigationCoordinator.showBottomSheet(screen)
                        }
                    )

                    SettingsHeader(
                        icon = Icons.Default.Preview,
                        title = LocalXmlStrings.current.createPostTabPreview,
                    )
                    // preview
                    ContentPreview(
                        postLayout = uiState.postLayout,
                        preferNicknames = uiState.preferUserNicknames,
                        showScores = uiState.voteFormat != VoteFormat.Hidden,
                        voteFormat = uiState.voteFormat,
                        fullHeightImage = uiState.fullHeightImages,
                        fullWidthImage = uiState.fullWidthImages,
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxxl))
                }
            }
        }
    }
}
