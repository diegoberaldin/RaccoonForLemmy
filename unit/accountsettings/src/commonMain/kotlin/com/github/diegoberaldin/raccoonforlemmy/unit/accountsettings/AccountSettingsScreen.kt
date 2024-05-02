package com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.text.font.FontFamily
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
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.EditFormattedInfoDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.EditTextualInfoDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AccountSettingsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<AccountSettingsMviModel>()
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
        var openDisplayNameEditDialog by remember { mutableStateOf(false) }
        var openEmailEditDialog by remember { mutableStateOf(false) }
        var openMatrixUserIdEditDialog by remember { mutableStateOf(false) }
        var openBioEditDialog by remember { mutableStateOf(false) }
        val successMessage = LocalXmlStrings.current.messageOperationSuccessful
        val errorMessage = LocalXmlStrings.current.messageGenericError
        val snackbarHostState = remember { SnackbarHostState() }
        val galleryHelper = remember { getGalleryHelper() }
        var openAvatarPicker by remember { mutableStateOf(false) }
        var openBannerPicker by remember { mutableStateOf(false) }
        var confirmBackWithUnsavedChangesDialog by remember { mutableStateOf(false) }

        LaunchedEffect(model) {
            model.effects.onEach { evt ->
                when (evt) {
                    AccountSettingsMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(errorMessage)
                    }

                    AccountSettingsMviModel.Effect.Success -> {
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
            modifier = Modifier
                .safeContentPadding()
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.settingsWebPreferences,
                        )
                    },
                    navigationIcon = {
                        if (navigationCoordinator.canPop.value) {
                            Image(
                                modifier = Modifier
                                    .onClick(
                                        onClick = rememberCallback {
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
                            animationSpec = InfiniteRepeatableSpec(
                                animation = tween(1000)
                            )
                        )
                        Icon(
                            modifier = Modifier
                                .padding(horizontal = Spacing.xs)
                                .then(
                                    if (!uiState.loading) {
                                        Modifier
                                    } else Modifier.rotate(iconRotate)
                                ),
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
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
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .then(
                        if (settings.hideNavigationBarWhileScrolling) {
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            Modifier
                        }
                    )
            ) {
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    SettingsHeader(
                        icon = Icons.Default.AccountCircle,
                        title = LocalXmlStrings.current.settingsWebHeaderPersonal,
                    )

                    // avatar
                    val avatarSize = IconSize.xxl
                    SettingsImageInfo(
                        title = LocalXmlStrings.current.settingsWebAvatar,
                        imageModifier = Modifier
                            .size(avatarSize)
                            .clip(RoundedCornerShape(avatarSize / 2)),
                        url = uiState.avatar,
                        onEdit = rememberCallback {
                            openAvatarPicker = true
                        }
                    )

                    // banner
                    SettingsImageInfo(
                        title = LocalXmlStrings.current.settingsWebBanner,
                        imageModifier = Modifier.fillMaxWidth().aspectRatio(3.5f),
                        contentScale = ContentScale.Crop,
                        url = uiState.banner,
                        onEdit = rememberCallback {
                            openBannerPicker = true
                        }
                    )

                    // display name
                    SettingsTextualInfo(
                        title = LocalXmlStrings.current.settingsWebDisplayName,
                        value = uiState.displayName,
                        valueStyle = contentTypography.bodyMedium,
                        onEdit = rememberCallback {
                            openDisplayNameEditDialog = true
                        },
                    )

                    // email
                    SettingsTextualInfo(
                        title = LocalXmlStrings.current.settingsWebEmail,
                        value = uiState.email,
                        valueStyle = contentTypography.bodyMedium,
                        onEdit = rememberCallback {
                            openEmailEditDialog = true
                        },
                    )

                    // Matrix user ID
                    SettingsTextualInfo(
                        title = LocalXmlStrings.current.settingsWebMatrix,
                        value = uiState.matrixUserId,
                        valueStyle = contentTypography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        onEdit = rememberCallback {
                            openMatrixUserIdEditDialog = true
                        },
                    )

                    // bio
                    SettingsFormattedInfo(
                        title = LocalXmlStrings.current.settingsWebBio,
                        value = uiState.bio,
                        onEdit = rememberCallback {
                            openBioEditDialog = true
                        },
                    )

                    // bots account
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsWebBot,
                        value = uiState.bot,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeBot(value))
                        },
                    )

                    SettingsHeader(
                        icon = Icons.AutoMirrored.Default.Article,
                        title = LocalXmlStrings.current.settingsWebHeaderContents,
                    )

                    // default listing type
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsDefaultListingType,
                        value = uiState.defaultListingType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = ListingTypeBottomSheet(
                                isLogged = true,
                                screenKey = "accountSettings",
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // default sort type
                    SettingsRow(
                        title = LocalXmlStrings.current.settingsDefaultPostSortType,
                        value = uiState.defaultSortType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = SortBottomSheet(
                                values = uiState.availableSortTypes.map { it.toInt() },
                                expandTop = true,
                                screenKey = "accountSettings",
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // show bots
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsWebShowBot,
                        value = uiState.showBotAccounts,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeShowBotAccounts(value))
                        },
                    )

                    // show NSFW
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsWebShowNsfw,
                        value = uiState.showNsfw,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeShowNsfw(value))
                        },
                    )

                    // show scores
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsShowScores,
                        value = uiState.showScores,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeShowScores(value))
                        },
                    )

                    // show read posts
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsWebShowRead,
                        value = uiState.showReadPosts,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeShowReadPosts(value))
                        },
                    )

                    SettingsHeader(
                        icon = Icons.Default.Notifications,
                        title = LocalXmlStrings.current.settingsWebHeaderNotifications,
                    )

                    // email notifications
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.settingsWebEmailNotifications,
                        value = uiState.sendNotificationsToEmail,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(
                                AccountSettingsMviModel.Intent.ChangeSendNotificationsToEmail(
                                    value,
                                )
                            )
                        },
                    )
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        enabled = uiState.hasUnsavedChanges,
                        onClick = {
                            model.reduce(AccountSettingsMviModel.Intent.Submit)
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.actionSave)
                    }
                }
            }
        }

        if (openDisplayNameEditDialog) {
            EditTextualInfoDialog(
                title = LocalXmlStrings.current.settingsWebDisplayName,
                value = uiState.displayName,
                onClose = rememberCallbackArgs(model) { newValue ->
                    openDisplayNameEditDialog = false
                    newValue?.also {
                        model.reduce(AccountSettingsMviModel.Intent.ChangeDisplayName(it))
                    }
                }
            )
        }

        if (openEmailEditDialog) {
            EditTextualInfoDialog(
                title = LocalXmlStrings.current.settingsWebEmail,
                value = uiState.email,
                onClose = rememberCallbackArgs(model) { newValue ->
                    openEmailEditDialog = false
                    newValue?.also {
                        model.reduce(AccountSettingsMviModel.Intent.ChangeEmail(it))
                    }
                }
            )
        }

        if (openMatrixUserIdEditDialog) {
            EditTextualInfoDialog(
                title = LocalXmlStrings.current.settingsWebMatrix,
                value = uiState.matrixUserId,
                onClose = rememberCallbackArgs(model) { newValue ->
                    openMatrixUserIdEditDialog = false
                    newValue?.also {
                        model.reduce(AccountSettingsMviModel.Intent.ChangeMatrixUserId(it))
                    }
                }
            )
        }

        if (openBioEditDialog) {
            EditFormattedInfoDialog(
                title = LocalXmlStrings.current.settingsWebBio,
                value = uiState.bio,
                onClose = rememberCallbackArgs(model) { newValue ->
                    openBioEditDialog = false
                    newValue?.also {
                        model.reduce(AccountSettingsMviModel.Intent.ChangeBio(it))
                    }
                },
            )
        }

        if (openAvatarPicker) {
            galleryHelper.getImageFromGallery { bytes ->
                openAvatarPicker = false
                model.reduce(AccountSettingsMviModel.Intent.AvatarSelected(bytes))
            }
        }
        if (openBannerPicker) {
            galleryHelper.getImageFromGallery { bytes ->
                openBannerPicker = false
                model.reduce(AccountSettingsMviModel.Intent.BannerSelected(bytes))
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
                        Text(text = LocalXmlStrings.current.buttonNoStay)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            confirmBackWithUnsavedChangesDialog = false
                            navigationCoordinator.popScreen()
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonYesQuit)
                    }
                },
                text = {
                    Text(text = LocalXmlStrings.current.messageUnsavedChanges)
                },
            )
        }
    }
}
