package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.getGalleryHelper
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CreatePostScreen(
    private val communityId: Int? = null,
    private val editedPost: PostModel? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel {
            getCreatePostViewModel(
                communityId = communityId,
                editedPostId = editedPost?.id,
            )
        }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val notificationCenter = remember { getNotificationCenter() }
        val galleryHelper = remember { getGalleryHelper() }

        LaunchedEffect(model) {
            model.reduce(CreatePostMviModel.Intent.SetTitle(editedPost?.title.orEmpty()))
            model.reduce(CreatePostMviModel.Intent.SetText(editedPost?.text.orEmpty()))

            model.effects.onEach {
                when (it) {
                    is CreatePostMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(it.message ?: genericError)
                    }

                    CreatePostMviModel.Effect.Success -> {
                        notificationCenter.getObserver(NotificationCenterContractKeys.PostCreated)
                            ?.also { o -> o.invoke(Unit) }
                        bottomSheetNavigator.hide()
                    }

                    else -> {}
                }
            }.launchIn(this)
        }

        Scaffold(topBar = {
            TopAppBar(
                title = {
                    Box {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                            verticalArrangement = Arrangement.spacedBy(Spacing.s),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BottomSheetHandle()
                            Text(
                                text = stringResource(MR.strings.create_post_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(content = {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                )
                            }, onClick = {
                                model.reduce(CreatePostMviModel.Intent.Send)
                            })
                        }
                    }
                },
            )
        }, snackbarHost = {
            SnackbarHost(snackbarHostState)
        }) { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                val bodyFocusRequester = remember { FocusRequester() }
                val urlFocusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                TextField(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                    label = {
                        Text(text = stringResource(MR.strings.create_post_name))
                    },
                    textStyle = MaterialTheme.typography.titleMedium,
                    value = uiState.title,
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

                var openImagePicker by remember { mutableStateOf(false) }
                if (openImagePicker) {
                    galleryHelper.getImageFromGallery { bytes ->
                        openImagePicker = false
                        model.reduce(CreatePostMviModel.Intent.ImageSelected(bytes))
                    }
                }

                TextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(urlFocusRequester),
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                    maxLines = 1,
                    label = {
                        Text(text = stringResource(MR.strings.create_post_url))
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.onClick {
                                openImagePicker = true
                            },
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = uiState.url,
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

                TextField(
                    modifier = Modifier.height(500.dp).fillMaxWidth()
                        .focusRequester(bodyFocusRequester),
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                    label = {
                        Text(text = stringResource(MR.strings.create_post_body))
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = uiState.body,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrect = false,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    onValueChange = { value ->
                        model.reduce(CreatePostMviModel.Intent.SetText(value))
                    },
                    isError = uiState.bodyError != null,
                    supportingText = {
                        if (uiState.bodyError != null) {
                            Text(
                                text = uiState.bodyError?.localized().orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                Spacer(Modifier.height(Spacing.xxl))
            }
        }

        if (uiState.loading) {
            ProgressHud()
        }
    }
}