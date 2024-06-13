package com.github.diegoberaldin.raccoonforlemmy.unit.editcommunity

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsFormattedInfo
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsImageInfo
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsTextualInfo
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.CommunityVisibilityBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.EditFormattedInfoDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.EditTextualInfoDialog
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

class EditCommunityScreen(
    private val communityId: Long? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model =
            getScreenModel<EditCommunityMviModel>(parameters = { parametersOf(communityId) })
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val scrollState = rememberScrollState()
        val themeRepository = remember { getThemeRepository() }
        val contentFontFamily by themeRepository.contentFontFamily.collectAsState()
        val contentTypography = contentFontFamily.toTypography()
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        var openNameEditDialog by remember { mutableStateOf(false) }
        var openTitleEditDialog by remember { mutableStateOf(false) }
        var openDescriptionEditDialog by remember { mutableStateOf(false) }
        val successMessage = LocalStrings.current.messageOperationSuccessful
        val errorMessage = LocalStrings.current.messageGenericError
        val snackbarHostState = remember { SnackbarHostState() }
        val galleryHelper = remember { getGalleryHelper() }
        var openIconPicker by remember { mutableStateOf(false) }
        var openBannerPicker by remember { mutableStateOf(false) }
        var confirmBackWithUnsavedChangesDialog by remember { mutableStateOf(false) }

        LaunchedEffect(model) {
            model.reduce(EditCommunityMviModel.Intent.Refresh)

            model.effects.onEach { evt ->
                when (evt) {
                    EditCommunityMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(errorMessage)
                    }

                    EditCommunityMviModel.Effect.Success -> {
                        snackbarHostState.showSnackbar(successMessage)
                    }
                }
            }.launchIn(this)
        }

        DisposableEffect(key) {
            navigationCoordinator.setCanGoBackCallback {
                if (uiState.hasUnsavedChanges) {
                    confirmBackWithUnsavedChangesDialog = true
                    return@setCanGoBackCallback false
                }
                true
            }
            onDispose {
                navigationCoordinator.setCanGoBackCallback(null)
            }
        }

        Scaffold(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text =
                                if (communityId == null) {
                                    LocalStrings.current.actionCreateCommunity
                                } else {
                                    buildString {
                                        append(LocalStrings.current.postActionEdit)
                                        append(" ")
                                        append(uiState.title)
                                    }
                                },
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    navigationIcon = {
                        if (navigationCoordinator.canPop.value) {
                            Image(
                                modifier =
                                    Modifier
                                        .onClick(
                                            onClick = {
                                                if (uiState.hasUnsavedChanges) {
                                                    confirmBackWithUnsavedChangesDialog = true
                                                } else {
                                                    navigationCoordinator.popScreen()
                                                }
                                            },
                                        ),
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                        }
                    },
                    actions = {
                        val transition = rememberInfiniteTransition()
                        val iconRotate by transition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec =
                                InfiniteRepeatableSpec(
                                    animation = tween(1000),
                                ),
                        )
                        Icon(
                            modifier =
                                Modifier
                                    .padding(horizontal = Spacing.xs)
                                    .then(
                                        if (!uiState.loading) {
                                            Modifier
                                        } else {
                                            Modifier.rotate(iconRotate)
                                        },
                                    ),
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
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
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = Spacing.m,
                        )
                        .then(
                            if (settings.hideNavigationBarWhileScrolling) {
                                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                Modifier
                            },
                        ),
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    if (communityId == null) {
                        SettingsHeader(
                            icon = Icons.Default.Badge,
                            title = LocalStrings.current.communityDetailInfo,
                        )

                        // name (handle prefix)
                        SettingsTextualInfo(
                            title = LocalStrings.current.multiCommunityEditorName,
                            value = uiState.name,
                            valueStyle = contentTypography.bodyMedium,
                            onEdit =
                                rememberCallback {
                                    openNameEditDialog = true
                                },
                        )
                    }

                    SettingsHeader(
                        icon = Icons.Default.Image,
                        title = LocalStrings.current.settingsTitlePictures,
                    )

                    // icon
                    val avatarSize = IconSize.xxl
                    SettingsImageInfo(
                        title = LocalStrings.current.multiCommunityEditorIcon,
                        imageModifier =
                            Modifier
                                .size(avatarSize)
                                .clip(RoundedCornerShape(avatarSize / 2)),
                        url = uiState.icon,
                        onEdit =
                            rememberCallback {
                                openIconPicker = true
                            },
                    )

                    // banner
                    SettingsImageInfo(
                        title = LocalStrings.current.settingsWebBanner,
                        imageModifier = Modifier.fillMaxWidth().aspectRatio(3.5f),
                        contentScale = ContentScale.Crop,
                        url = uiState.banner,
                        onEdit =
                            rememberCallback {
                                openBannerPicker = true
                            },
                    )

                    SettingsHeader(
                        icon = Icons.Default.TextFormat,
                        title = LocalStrings.current.editCommunityHeaderTextual,
                    )

                    // display name
                    SettingsTextualInfo(
                        title = LocalStrings.current.settingsWebDisplayName,
                        value = uiState.title,
                        valueStyle = contentTypography.bodyMedium,
                        onEdit =
                            rememberCallback {
                                openTitleEditDialog = true
                            },
                    )
                    // sidebar
                    SettingsFormattedInfo(
                        title = LocalStrings.current.editCommunityItemSidebar,
                        value = uiState.description,
                        onEdit =
                            rememberCallback {
                                openDescriptionEditDialog = true
                            },
                    )

                    SettingsHeader(
                        icon = Icons.Default.SettingsApplications,
                        title = LocalStrings.current.navigationSettings,
                    )

                    SettingsSwitchRow(
                        title = LocalStrings.current.createPostNsfw,
                        value = uiState.nsfw,
                        onValueChanged =
                            rememberCallbackArgs(model) { value ->
                                model.reduce(EditCommunityMviModel.Intent.ChangeNsfw(value))
                            },
                    )
                    SettingsSwitchRow(
                        title = LocalStrings.current.editCommunityItemPostingRestrictedToMods,
                        value = uiState.postingRestrictedToMods,
                        onValueChanged =
                            rememberCallbackArgs(model) { value ->
                                model.reduce(
                                    EditCommunityMviModel.Intent.ChangePostingRestrictedToMods(
                                        value,
                                    ),
                                )
                            },
                    )

                    SettingsRow(
                        title = LocalStrings.current.editCommunityItemVisibility,
                        value = uiState.visibilityType.toReadableName(),
                        onTap =
                            rememberCallback {
                                val sheet = CommunityVisibilityBottomSheet()
                                navigationCoordinator.showBottomSheet(sheet)
                            },
                    )

                    Spacer(modifier = Modifier.height(Spacing.m))
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        enabled = uiState.hasUnsavedChanges,
                        onClick = {
                            model.reduce(EditCommunityMviModel.Intent.Submit)
                        },
                    ) {
                        Text(text = LocalStrings.current.actionSave)
                    }
                }
            }
        }

        if (openNameEditDialog) {
            EditTextualInfoDialog(
                title = LocalStrings.current.multiCommunityEditorName,
                value = uiState.title,
                onClose =
                    rememberCallbackArgs(model) { newValue ->
                        openNameEditDialog = false
                        newValue?.also {
                            model.reduce(EditCommunityMviModel.Intent.ChangeName(it))
                        }
                    },
            )
        }

        if (openTitleEditDialog) {
            EditTextualInfoDialog(
                title = LocalStrings.current.settingsWebDisplayName,
                value = uiState.title,
                onClose =
                    rememberCallbackArgs(model) { newValue ->
                        openTitleEditDialog = false
                        newValue?.also {
                            model.reduce(EditCommunityMviModel.Intent.ChangeTitle(it))
                        }
                    },
            )
        }

        if (openDescriptionEditDialog) {
            EditFormattedInfoDialog(
                title = LocalStrings.current.settingsWebBio,
                value = uiState.description,
                onClose =
                    rememberCallbackArgs(model) { newValue ->
                        openDescriptionEditDialog = false
                        newValue?.also {
                            model.reduce(EditCommunityMviModel.Intent.ChangeDescription(it))
                        }
                    },
            )
        }

        if (openIconPicker) {
            galleryHelper.getImageFromGallery { bytes ->
                openIconPicker = false
                model.reduce(EditCommunityMviModel.Intent.IconSelected(bytes))
            }
        }
        if (openBannerPicker) {
            galleryHelper.getImageFromGallery { bytes ->
                openBannerPicker = false
                model.reduce(EditCommunityMviModel.Intent.BannerSelected(bytes))
            }
        }

        if (confirmBackWithUnsavedChangesDialog) {
            AlertDialog(
                onDismissRequest = {
                    confirmBackWithUnsavedChangesDialog = false
                },
                dismissButton = {
                    Button(
                        onClick = {
                            confirmBackWithUnsavedChangesDialog = false
                        },
                    ) {
                        Text(text = LocalStrings.current.buttonNoStay)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            confirmBackWithUnsavedChangesDialog = false
                            navigationCoordinator.popScreen()
                        },
                    ) {
                        Text(text = LocalStrings.current.buttonYesQuit)
                    }
                },
                text = {
                    Text(text = LocalStrings.current.messageUnsavedChanges)
                },
            )
        }
    }
}
