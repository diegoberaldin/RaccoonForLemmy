package com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.components.AccountSettingsFormattedInfo
import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.components.AccountSettingsImageInfo
import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.components.AccountSettingsTextualInfo
import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.components.EditFormattedInfoDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.components.EditTextualInfoDialog
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AccountSettingsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<AccountSettingsMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val scrollState = rememberScrollState()
        val themeRepository = remember { getThemeRepository() }
        val contentFontFamily by themeRepository.contentFontFamily.collectAsState()
        val contentTypography = contentFontFamily.toTypography()
        var openDisplayNameEditDialog by remember { mutableStateOf(false) }
        var openEmailEditDialog by remember { mutableStateOf(false) }
        var openMatrixUserIdEditDialog by remember { mutableStateOf(false) }
        var openBioEditDialog by remember { mutableStateOf(false) }
        val successMessage = stringResource(MR.strings.message_operation_successful)
        val errorMessage = stringResource(MR.strings.message_generic_error)
        val snackbarHostState = remember { SnackbarHostState() }
        val galleryHelper = remember { getGalleryHelper() }
        var openAvatarPicker by remember { mutableStateOf(false) }
        var openBannerPicker by remember { mutableStateOf(false) }

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

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = stringResource(MR.strings.settings_web_preferences),
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
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                        }
                    },
                    actions = {
                        Icon(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    model.reduce(AccountSettingsMviModel.Intent.Submit)
                                },
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
            Box(
                modifier = Modifier.padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    SettingsHeader(
                        icon = Icons.Default.AccountCircle,
                        title = stringResource(MR.strings.settings_web_header_personal),
                    )

                    // avatar
                    val avatarSize = IconSize.xxl
                    AccountSettingsImageInfo(
                        title = stringResource(MR.strings.settings_web_avatar),
                        imageModifier = Modifier
                            .size(avatarSize)
                            .clip(RoundedCornerShape(avatarSize / 2)),
                        url = uiState.avatar,
                        onEdit = rememberCallback {
                            openAvatarPicker = true
                        }
                    )

                    // banner
                    AccountSettingsImageInfo(
                        title = stringResource(MR.strings.settings_web_banner),
                        imageModifier = Modifier.fillMaxWidth().aspectRatio(3.5f),
                        url = uiState.banner,
                        onEdit = rememberCallback {
                            openBannerPicker = true
                        }
                    )

                    // display name
                    AccountSettingsTextualInfo(
                        title = stringResource(MR.strings.settings_web_display_name),
                        value = uiState.displayName,
                        valueStyle = contentTypography.bodyMedium,
                        onEdit = rememberCallback {
                            openDisplayNameEditDialog = true
                        },
                    )

                    // email
                    AccountSettingsTextualInfo(
                        title = stringResource(MR.strings.settings_web_email),
                        value = uiState.email,
                        valueStyle = contentTypography.bodyMedium,
                        onEdit = rememberCallback {
                            openEmailEditDialog = true
                        },
                    )

                    // Matrix user ID
                    AccountSettingsTextualInfo(
                        title = stringResource(MR.strings.settings_web_matrix),
                        value = uiState.matrixUserId,
                        valueStyle = contentTypography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                        ),
                        onEdit = rememberCallback {
                            openMatrixUserIdEditDialog = true
                        },
                    )

                    // bio
                    AccountSettingsFormattedInfo(
                        title = stringResource(MR.strings.settings_web_bio),
                        value = uiState.bio,
                        onEdit = rememberCallback {
                            openBioEditDialog = true
                        },
                    )

                    // show bots
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_web_bot),
                        value = uiState.bot,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeBot(value))
                        },
                    )

                    SettingsHeader(
                        icon = Icons.Default.Dashboard,
                        title = stringResource(MR.strings.settings_web_header_contents),
                    )

                    // default listing type
                    SettingsRow(
                        title = stringResource(MR.strings.settings_default_listing_type),
                        value = uiState.defaultListingType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = ListingTypeBottomSheet(
                                sheetKey = key,
                                isLogged = true,
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // default sort type
                    SettingsRow(
                        title = stringResource(MR.strings.settings_default_post_sort_type),
                        value = uiState.defaultSortType.toReadableName(),
                        onTap = rememberCallback {
                            val sheet = SortBottomSheet(
                                sheetKey = key,
                                values = uiState.availableSortTypes,
                                expandTop = true,
                                comments = false,
                            )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    )

                    // show bots
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_web_show_bot),
                        value = uiState.showBotAccounts,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeShowBotAccounts(value))
                        },
                    )

                    // show NSFW
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_web_show_nsfw),
                        value = uiState.showNsfw,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeShowNsfw(value))
                        },
                    )

                    // show read posts
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_web_show_read),
                        value = uiState.showReadPosts,
                        onValueChanged = rememberCallbackArgs { value ->
                            model.reduce(AccountSettingsMviModel.Intent.ChangeShowReadPosts(value))
                        },
                    )

                    SettingsHeader(
                        icon = Icons.Default.Notifications,
                        title = stringResource(MR.strings.settings_web_header_notifications),
                    )

                    // email notifications
                    SettingsSwitchRow(
                        title = stringResource(MR.strings.settings_web_email_notifications),
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
            }
        }

        if (openDisplayNameEditDialog) {
            EditTextualInfoDialog(
                title = stringResource(MR.strings.settings_web_display_name),
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
                title = stringResource(MR.strings.settings_web_email),
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
                title = stringResource(MR.strings.settings_web_matrix),
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
                title = stringResource(MR.strings.settings_web_bio),
                value = uiState.bio,
                onClose = rememberCallbackArgs(model) { newValue ->
                    openBioEditDialog = false
                    newValue?.also {
                        model.reduce(AccountSettingsMviModel.Intent.ChangeBio(it))
                    }
                },
            )
        }

        if (uiState.loading) {
            ProgressHud()
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
    }
}
