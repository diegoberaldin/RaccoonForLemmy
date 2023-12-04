package com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.TextFormattingBar
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getInboxChatViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class InboxChatScreen(
    private val otherUserId: Int,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getInboxChatViewModel(otherUserId) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val focusManager = LocalFocusManager.current
        val galleryHelper = remember { getGalleryHelper() }
        var openImagePicker by remember { mutableStateOf(false) }
        var textFieldValue by remember {
            mutableStateOf(
                TextFieldValue(text = "")
            )
        }

        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    is InboxChatMviModel.Effect.AddImageToText -> {
                        textFieldValue = textFieldValue.let {
                            it.copy(text = it.text + "\n![](${effect.url})")
                        }
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val avatar = uiState.otherUserAvatar.orEmpty()
                            if (avatar.isNotEmpty()) {
                                CustomImage(
                                    modifier = Modifier.padding(Spacing.xxxs).size(IconSize.s)
                                        .clip(RoundedCornerShape(IconSize.s / 2)),
                                    url = avatar,
                                    autoload = uiState.autoLoadImages,
                                    quality = FilterQuality.Low,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                )
                            }
                            Text(
                                modifier = Modifier.padding(horizontal = Spacing.s),
                                text = uiState.otherUserName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    navigationIcon = {
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
                    },
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier.padding(bottom = Spacing.s),
                ) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        label = {
                            Text(text = stringResource(MR.strings.inbox_chat_message))
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    model.reduce(
                                        InboxChatMviModel.Intent.SubmitNewMessage(
                                            textFieldValue.text
                                        )
                                    )
                                    textFieldValue = TextFieldValue(text = "")
                                    focusManager.clearFocus()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                )
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        value = textFieldValue,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            autoCorrect = true,
                            imeAction = ImeAction.Next,
                        ),
                        onValueChange = { value ->
                            textFieldValue = value
                        },
                        supportingText = {
                            TextFormattingBar(
                                textFieldValue = textFieldValue,
                                onTextFieldValueChanged = {
                                    textFieldValue = it
                                },
                                onSelectImage = {
                                    openImagePicker = true
                                }
                            )
                        },
                    )
                }
            }
        ) { padding ->
            if (uiState.currentUserId != null) {
                Box(
                    modifier = Modifier.padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        reverseLayout = true,
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(Spacing.s))
                        }
                        items(uiState.messages) { message ->
                            val isMyMessage = message.creator?.id == uiState.currentUserId
                            val content = message.content.orEmpty()
                            val date = message.publishDate.orEmpty()
                            MessageCard(
                                isMyMessage = isMyMessage,
                                content = content,
                                date = date,
                            )
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(InboxChatMviModel.Intent.LoadNextPage)
                            }
                            if (uiState.loading && !uiState.refreshing) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.xs),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(25.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(Spacing.xxxl))
                        }
                    }
                }
            }

            if (openImagePicker) {
                galleryHelper.getImageFromGallery { bytes ->
                    openImagePicker = false
                    model.reduce(InboxChatMviModel.Intent.ImageSelected(bytes))
                }
            }
        }
    }
}
