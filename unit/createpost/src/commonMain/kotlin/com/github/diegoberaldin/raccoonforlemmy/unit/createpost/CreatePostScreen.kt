package com.github.diegoberaldin.raccoonforlemmy.unit.createpost

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CreatePostSection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.TextFormattingBar
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SelectLanguageDialog
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.SelectCommunityDialog
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

class CreatePostScreen(
    private val communityId: Int? = null,
    private val editedPostId: Int? = null,
    private val crossPostId: Int? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<CreatePostMviModel> {
            parametersOf(editedPostId, crossPostId)
        }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)
        val notificationCenter = remember { getNotificationCenter() }
        val galleryHelper = remember { getGalleryHelper() }
        val crossPostText = stringResource(MR.strings.create_post_cross_post_text)
        val crossPost = uiState.crossPost
        val editedPost = uiState.editedPost
        var bodyTextFieldValue by remember(crossPost, editedPost) {
            val text = buildString {
                if (crossPost != null) {
                    append(crossPostText)
                    append(" ")
                    append(crossPost.originalUrl)
                } else if (editedPost != null) {
                    append(editedPost.text)
                }
            }
            mutableStateOf(TextFieldValue(text = text))
        }

        val bodyFocusRequester = remember { FocusRequester() }
        val urlFocusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val navigationCoordinator = remember { getNavigationCoordinator() }
        var openImagePicker by remember { mutableStateOf(false) }
        var openImagePickerInBody by remember { mutableStateOf(false) }
        if (openImagePicker) {
            galleryHelper.getImageFromGallery { bytes ->
                openImagePicker = false
                model.reduce(CreatePostMviModel.Intent.ImageSelected(bytes))
            }
        }
        if (openImagePickerInBody) {
            galleryHelper.getImageFromGallery { bytes ->
                openImagePickerInBody = false
                model.reduce(CreatePostMviModel.Intent.InsertImageInBody(bytes))
            }
        }

        var openSelectCommunity by remember { mutableStateOf(false) }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
        val themeRepository = remember { getThemeRepository() }
        val contentFontFamily by themeRepository.contentFontFamily.collectAsState()
        val typography = contentFontFamily.toTypography()
        var selectLanguageDialogOpen by remember { mutableStateOf(false) }

        LaunchedEffect(editedPost) {
            editedPost?.community?.id?.also { communityId ->
                model.reduce(
                    CreatePostMviModel.Intent.SetCommunity(
                        CommunityModel(id = communityId)
                    )
                )
            }
            val referencePost = uiState.editedPost ?: uiState.crossPost
            model.reduce(CreatePostMviModel.Intent.SetTitle(referencePost?.title.orEmpty()))
            model.reduce(CreatePostMviModel.Intent.SetUrl(referencePost?.url.orEmpty()))
            model.reduce(CreatePostMviModel.Intent.ChangeLanguage(referencePost?.languageId))
        }

        LaunchedEffect(model) {
            communityId?.also { communityId ->
                CreatePostMviModel.Intent.SetCommunity(CommunityModel(id = communityId))
            }

            model.effects.onEach { effect ->
                when (effect) {
                    is CreatePostMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(effect.message ?: genericError)
                    }

                    CreatePostMviModel.Effect.Success -> {
                        notificationCenter.send(
                            event = NotificationCenterEvent.PostCreated,
                        )
                        navigationCoordinator.popScreen()
                    }

                    is CreatePostMviModel.Effect.AddImageToBody -> {
                        bodyTextFieldValue = bodyTextFieldValue.let {
                            it.copy(text = it.text + "\n![](${effect.url})")
                        }
                    }
                }
            }.launchIn(this)
        }
        LaunchedEffect(notificationCenter)
        {
            notificationCenter.subscribe(NotificationCenterEvent.SelectCommunity::class)
                .onEach { evt ->
                    model.reduce(CreatePostMviModel.Intent.SetCommunity(evt.model))
                    focusManager.clearFocus()
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.CloseDialog::class).onEach {
                if (openSelectCommunity) {
                    openSelectCommunity = false
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.padding(start = Spacing.s).onClick(
                                onClick = rememberCallback {
                                    navigationCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(
                                top = Spacing.s,
                                start = Spacing.l,
                            ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.s),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BottomSheetHandle()
                            Text(
                                text = when {
                                    editedPost != null -> stringResource(MR.strings.edit_post_title)
                                    else -> stringResource(MR.strings.create_post_title)
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                )
                            },
                            onClick = rememberCallback(model, bodyTextFieldValue) {
                                model.reduce(CreatePostMviModel.Intent.Send(bodyTextFieldValue.text))
                            },
                        )
                    },
                )
            }, snackbarHost =
            {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        snackbarData = data,
                    )
                }
            })
        { padding ->
            Column(
                modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()),
            ) {
                // community
                if (crossPost != null) {
                    TextField(
                        modifier = Modifier.fillMaxWidth().onFocusChanged(
                            rememberCallbackArgs { state ->
                                if (state.hasFocus) {
                                    openSelectCommunity = true
                                }
                            },
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        label = {
                            Text(
                                text = stringResource(MR.strings.create_post_community),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                            )
                        },
                        textStyle = typography.bodyMedium,
                        value = uiState.communityInfo,
                        readOnly = true,
                        singleLine = true,
                        onValueChange = {},
                        isError = uiState.communityError != null,
                        supportingText = {
                            if (uiState.communityError != null) {
                                Text(
                                    text = uiState.communityError?.localized().orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                    )
                }

                // title
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    label = {
                        Text(
                            text = stringResource(MR.strings.create_post_name),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    textStyle = typography.titleMedium,
                    value = uiState.title,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrect = true,
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        urlFocusRequester.requestFocus()
                    }),
                    onValueChange = rememberCallbackArgs(model) { value ->
                        model.reduce(CreatePostMviModel.Intent.SetTitle(value))
                    },
                    isError = uiState.titleError != null,
                    supportingText = {
                        if (uiState.titleError != null) {
                            Text(
                                text = uiState.titleError?.localized().orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                // image
                TextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(urlFocusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    label = {
                        Text(
                            text = stringResource(MR.strings.create_post_url),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    openImagePicker = true
                                },
                            ),
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                        )
                    },
                    textStyle = typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                    value = uiState.url,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrect = false,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        bodyFocusRequester.requestFocus()
                    }),
                    onValueChange = rememberCallbackArgs(model) { value ->
                        model.reduce(CreatePostMviModel.Intent.SetUrl(value))
                    },
                    isError = uiState.urlError != null,
                    supportingText = {
                        if (uiState.urlError != null) {
                            Text(
                                text = uiState.urlError?.localized().orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                // NSFW
                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        vertical = Spacing.s, horizontal = Spacing.m
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(MR.strings.create_post_nsfw),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = uiState.nsfw,
                        onCheckedChange = rememberCallbackArgs(model) { it ->
                            model.reduce(CreatePostMviModel.Intent.ChangeNsfw(it))
                        },
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    SectionSelector(
                        titles = listOf(
                            stringResource(MR.strings.create_post_tab_editor),
                            stringResource(MR.strings.create_post_tab_preview),
                        ),
                        currentSection = when (uiState.section) {
                            CreatePostSection.Preview -> 1
                            else -> 0
                        },
                        onSectionSelected = rememberCallbackArgs(model) { it ->
                            val section = when (it) {
                                1 -> CreatePostSection.Preview
                                else -> CreatePostSection.Edit
                            }
                            model.reduce(CreatePostMviModel.Intent.ChangeSection(section))
                        },
                    )

                    if (uiState.section == CreatePostSection.Edit) {
                        TextFormattingBar(
                            modifier = Modifier.padding(
                                top = Spacing.s,
                                start = Spacing.m,
                                end = Spacing.m,
                            ),
                            textFieldValue = bodyTextFieldValue,
                            onTextFieldValueChanged = {
                                bodyTextFieldValue = it
                            },
                            onSelectImage = {
                                openImagePickerInBody = true
                            },
                            currentLanguageId = uiState.currentLanguageId,
                            availableLanguages = uiState.availableLanguages,
                            onSelectLanguage = {
                                selectLanguageDialogOpen = true
                            },
                        )
                        TextField(
                            modifier = Modifier
                                .heightIn(min = 300.dp, max = 400.dp)
                                .fillMaxWidth()
                                .focusRequester(bodyFocusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                            label = {
                                Text(
                                    text = stringResource(MR.strings.create_post_body),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            },
                            textStyle = typography.bodyMedium,
                            value = bodyTextFieldValue,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                autoCorrect = true,
                                capitalization = KeyboardCapitalization.Sentences,
                            ),
                            onValueChange = { value ->
                                bodyTextFieldValue = value
                            },
                            isError = uiState.bodyError != null,
                            supportingText = {
                                Column(
                                    modifier = Modifier.padding(bottom = Spacing.xxs),
                                ) {
                                    if (uiState.bodyError != null) {
                                        Text(
                                            text = uiState.bodyError?.localized().orEmpty(),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    if (uiState.currentUser.isNotEmpty()) {
                                        Text(
                                            text = buildString {
                                                append(stringResource(MR.strings.post_reply_source_account))
                                                append(" ")
                                                append(uiState.currentUser)
                                                if (uiState.currentInstance.isNotEmpty()) {
                                                    append("@")
                                                    append(uiState.currentInstance)
                                                }
                                            },
                                            color = MaterialTheme.colorScheme.onBackground,
                                            style = MaterialTheme.typography.labelSmall,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    }
                                }
                            },
                        )
                    } else {
                        val post = PostModel(
                            text = bodyTextFieldValue.text,
                            title = uiState.title,
                            url = uiState.url,
                            thumbnailUrl = uiState.url,
                        )

                        PostCard(
                            post = post,
                            postLayout = uiState.postLayout,
                            fullHeightImage = uiState.fullHeightImages,
                            includeFullBody = true,
                            voteFormat = uiState.voteFormat,
                            autoLoadImages = uiState.autoLoadImages,
                            preferNicknames = uiState.preferNicknames,
                            showScores = uiState.showScores,
                        )
                    }
                }
            }
        }

        if (selectLanguageDialogOpen) {
            SelectLanguageDialog(
                languages = uiState.availableLanguages,
                currentLanguageId = uiState.currentLanguageId,
                onSelect = rememberCallbackArgs { langId ->
                    model.reduce(CreatePostMviModel.Intent.ChangeLanguage(langId))
                    selectLanguageDialogOpen = false
                },
                onDismiss = rememberCallback {
                    selectLanguageDialogOpen = false
                }
            )
        }

        if (uiState.loading) {
            ProgressHud()
        }

        if (openSelectCommunity) {
            SelectCommunityDialog().Content()
        }
    }
}