package com.github.diegoberaldin.raccoonforlemmy.unit.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.TextFormattingBar
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.unit.chat.components.MessageCard
import com.github.diegoberaldin.raccoonforlemmy.unit.chat.components.MessageCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

class InboxChatScreen(
    private val otherUserId: Long,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<InboxChatMviModel> { parametersOf(otherUserId) }
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val galleryHelper = remember { getGalleryHelper() }
        var openImagePicker by remember { mutableStateOf(false) }
        var textFieldValue by remember {
            mutableStateOf(
                TextFieldValue(text = ""),
            )
        }
        val themeRepository = remember { getThemeRepository() }
        val contentFontFamily by themeRepository.contentFontFamily.collectAsState()
        val typography = contentFontFamily.toTypography()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val lazyListState = rememberLazyListState()
        val detailOpener = remember { getDetailOpener() }
        var itemIdToDelete by remember { mutableStateOf<Long?>(null) }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    is InboxChatMviModel.Effect.AddImageToText -> {
                        textFieldValue = textFieldValue.let {
                            it.copy(text = it.text + "\n![](${effect.url})")
                        }
                    }

                    InboxChatMviModel.Effect.ScrollToBottom -> {
                        runCatching {
                            lazyListState.scrollToItem(0)
                        }
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                                    contentScale = ContentScale.FillBounds,
                                )
                            }
                            Text(
                                modifier = Modifier.padding(horizontal = Spacing.s),
                                text = uiState.otherUserName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    },
                    navigationIcon = {
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
                    },
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier.padding(
                        top = Spacing.xs,
                        bottom = Spacing.l,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    TextFormattingBar(
                        modifier = Modifier.padding(horizontal = Spacing.s),
                        textFieldValue = textFieldValue,
                        onTextFieldValueChanged = {
                            textFieldValue = it
                        },
                        onSelectImage = {
                            openImagePicker = true
                        },
                        lastActionIcon = Icons.AutoMirrored.Default.Send,
                        onLastAction = rememberCallback {
                            model.reduce(
                                InboxChatMviModel.Intent.SubmitNewMessage(
                                    textFieldValue.text,
                                ),
                            )
                            textFieldValue = TextFieldValue(text = "")
                        },
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .heightIn(
                                min = 80.dp,
                                max = 360.dp,
                            ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        label = {
                            Text(
                                text = buildString {
                                    if (uiState.editedMessageId != null) {
                                        append(LocalXmlStrings.current.inboxChatMessage)
                                        append(" (")
                                        append(LocalXmlStrings.current.postActionEdit)
                                        append(")")
                                    } else {
                                        append(LocalXmlStrings.current.actionChat)
                                    }
                                },
                                style = typography.bodyMedium,
                            )
                        },
                        textStyle = typography.bodyMedium,
                        value = textFieldValue,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            autoCorrect = true,
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                        onValueChange = { value ->
                            textFieldValue = value
                        },
                    )
                }
            },
        ) { padding ->
            if (uiState.currentUserId != null) {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true,
                        state = lazyListState,
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(Spacing.s))
                        }
                        if (uiState.messages.isEmpty() && uiState.initial) {
                            items(5) {
                                MessageCardPlaceholder()
                            }
                        }
                        items(
                            items = uiState.messages,
                            key = { it.id.toString() + (it.updateDate ?: it.publishDate) },
                        ) { message ->
                            val isMyMessage = message.creator?.id == uiState.currentUserId
                            val content = message.content.orEmpty()
                            val date = message.publishDate.orEmpty()
                            MessageCard(
                                isMyMessage = isMyMessage,
                                content = content,
                                date = date,
                                onOpenImage = rememberCallbackArgs { url ->
                                    navigationCoordinator.pushScreen(
                                        ZoomableImageScreen(
                                            url = url,
                                            source = message.creator?.readableHandle.orEmpty(),
                                        ),
                                    )
                                },
                                onOpenCommunity = rememberCallbackArgs { community, instance ->
                                    detailOpener.openCommunityDetail(
                                        community,
                                        instance,
                                    )
                                },
                                onOpenUser = rememberCallbackArgs { user, instance ->
                                    detailOpener.openUserDetail(user, instance)
                                },
                                onOpenPost = rememberCallbackArgs { post, instance ->
                                    detailOpener.openPostDetail(
                                        post = post,
                                        otherInstance = instance,
                                    )
                                },
                                onOpenWeb = rememberCallbackArgs { url ->
                                    navigationCoordinator.pushScreen(
                                        WebViewScreen(url),
                                    )
                                },
                                options = buildList {
                                    this += Option(
                                        OptionId.SeeRaw,
                                        LocalXmlStrings.current.postActionSeeRaw,
                                    )
                                    if (isMyMessage) {
                                        this += Option(
                                            OptionId.Edit,
                                            LocalXmlStrings.current.postActionEdit,
                                        )
                                        this += Option(
                                            OptionId.Delete,
                                            LocalXmlStrings.current.commentActionDelete,
                                        )
                                    }
                                },
                                onOptionSelected = rememberCallbackArgs { optionId ->
                                    when (optionId) {
                                        OptionId.Edit -> {
                                            model.reduce(
                                                InboxChatMviModel.Intent.EditMessage(message.id),
                                            )
                                            message.content?.also {
                                                textFieldValue = TextFieldValue(text = it)
                                            }
                                        }

                                        OptionId.SeeRaw -> {
                                            rawContent = message
                                        }

                                        OptionId.Delete -> {
                                            itemIdToDelete = message.id
                                        }

                                        else -> Unit
                                    }
                                },
                            )
                            Spacer(modifier = Modifier.height(Spacing.s))
                        }
                        item {
                            if (!uiState.initial && !uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(InboxChatMviModel.Intent.LoadNextPage)
                            }
                            if (!uiState.initial && uiState.loading && !uiState.refreshing) {
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

        if (rawContent != null) {
            when (val content = rawContent) {
                is PrivateMessageModel -> {
                    RawContentDialog(
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        text = content.content,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }
            }
        }

        itemIdToDelete?.also { itemId ->
            AlertDialog(
                onDismissRequest = {
                    itemIdToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            itemIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(InboxChatMviModel.Intent.DeleteMessage(itemId))
                            itemIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonConfirm)
                    }
                },
                text = {
                    Text(text = LocalXmlStrings.current.messageAreYouSure)
                },
            )
        }
    }
}
