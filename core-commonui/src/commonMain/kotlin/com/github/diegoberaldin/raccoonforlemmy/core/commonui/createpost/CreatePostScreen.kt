package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.TextFormattingBar
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.selectcommunity.SelectCommunityDialog
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.shareUrl
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CreatePostScreen(
    private val communityId: Int? = null,
    private val editedPost: PostModel? = null,
    private val crossPost: PostModel? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel {
            getCreatePostViewModel(editedPostId = editedPost?.id)
        }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)
        val notificationCenter = remember { getNotificationCenter() }
        val galleryHelper = remember { getGalleryHelper() }
        val crossPostText = stringResource(MR.strings.create_post_cross_post_text)
        var bodyTextFieldValue by remember {
            val text = when {
                crossPost != null -> buildString {
                    append(crossPostText)
                    append(" ")
                    append(crossPost.shareUrl)
                }

                editedPost != null -> {
                    editedPost.text
                }

                else -> ""
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
        val keyboardScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    focusManager.clearFocus()
                    return Offset.Zero
                }
            }
        }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

        LaunchedEffect(model) {
            val referencePost = editedPost ?: crossPost
            model.reduce(CreatePostMviModel.Intent.SetTitle(referencePost?.title.orEmpty()))
            model.reduce(CreatePostMviModel.Intent.SetUrl(referencePost?.url.orEmpty()))
            if (communityId != null) {
                model.reduce(
                    CreatePostMviModel.Intent.SetCommunity(CommunityModel(id = communityId))
                )
            }

            model.effects.onEach { effect ->
                when (effect) {
                    is CreatePostMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(effect.message ?: genericError)
                    }

                    CreatePostMviModel.Effect.Success -> {
                        notificationCenter.getObserver(NotificationCenterContractKeys.PostCreated)
                            ?.also { o -> o.invoke(Unit) }
                        navigationCoordinator.getBottomNavigator()?.hide()
                    }

                    is CreatePostMviModel.Effect.AddImageToBody -> {
                        bodyTextFieldValue = bodyTextFieldValue.let {
                            it.copy(text = it.text + "\n![](${effect.url})")
                        }
                    }
                }
            }.launchIn(this)
        }
        DisposableEffect(key) {
            notificationCenter.addObserver(
                {
                    (it as CommunityModel)?.also { community ->
                        model.reduce(CreatePostMviModel.Intent.SetCommunity(community))
                        focusManager.clearFocus()
                    }
                }, key, NotificationCenterContractKeys.SelectCommunity
            )

            notificationCenter.addObserver(
                {
                    if (openSelectCommunity) {
                        openSelectCommunity = false
                    }
                }, key, NotificationCenterContractKeys.CloseDialog
            )

            onDispose {
                notificationCenter.removeObserver(key)
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(keyboardScrollConnection),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = Spacing.s,
                                    start = Spacing.l,
                                ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.s),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BottomSheetHandle()
                            Text(
                                text = when {
                                    else ->
                                        stringResource(MR.strings.create_post_title)
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
                            onClick = rememberCallback(model) {
                                model.reduce(CreatePostMviModel.Intent.Send(bodyTextFieldValue.text))
                            },
                        )
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                // community
                if (crossPost != null) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged(
                                rememberCallbackArgs {
                                    if (it.hasFocus) {
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
                            Text(text = stringResource(MR.strings.create_post_community))
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
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
                        Text(text = stringResource(MR.strings.create_post_name))
                    },
                    textStyle = MaterialTheme.typography.titleMedium,
                    value = uiState.title,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrect = false,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        urlFocusRequester.requestFocus()
                    }),
                    onValueChange = { value ->
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
                        Text(text = stringResource(MR.strings.create_post_url))
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.onClick(
                                rememberCallback {
                                    openImagePicker = true
                                },
                            ),
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
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
                    onValueChange = { value ->
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
                    Switch(checked = uiState.nsfw, onCheckedChange = {
                        model.reduce(CreatePostMviModel.Intent.ChangeNsfw(it))
                    })
                }

                SectionSelector(
                    titles = listOf(
                        stringResource(MR.strings.create_post_tab_editor),
                        stringResource(MR.strings.create_post_tab_preview),
                    ),
                    currentSection = when (uiState.section) {
                        CreatePostSection.Preview -> 1
                        else -> 0
                    },
                    onSectionSelected = {
                        val section = when (it) {
                            1 -> CreatePostSection.Preview
                            else -> CreatePostSection.Edit
                        }
                        model.reduce(CreatePostMviModel.Intent.ChangeSection(section))
                    }
                )

                if (uiState.section == CreatePostSection.Edit) {
                    TextField(
                        modifier = Modifier
                            .height(500.dp)
                            .fillMaxWidth()
                            .focusRequester(bodyFocusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        label = {
                            Text(text = stringResource(MR.strings.create_post_body))
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        value = bodyTextFieldValue,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            autoCorrect = true,
                        ),
                        onValueChange = { value ->
                            bodyTextFieldValue = value
                        },
                        isError = uiState.bodyError != null,
                        supportingText = {
                            Column {
                                if (uiState.bodyError != null) {
                                    Text(
                                        text = uiState.bodyError?.localized().orEmpty(),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                                TextFormattingBar(
                                    textFieldValue = bodyTextFieldValue,
                                    onTextFieldValueChanged = {
                                        bodyTextFieldValue = it
                                    },
                                    onSelectImage = {
                                        openImagePickerInBody = true
                                    }
                                )
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
                        separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                        autoLoadImages = uiState.autoLoadImages,
                    )
                }
            }
        }

        if (uiState.loading) {
            ProgressHud()
        }

        if (openSelectCommunity) {
            SelectCommunityDialog().Content()
        }
    }
}