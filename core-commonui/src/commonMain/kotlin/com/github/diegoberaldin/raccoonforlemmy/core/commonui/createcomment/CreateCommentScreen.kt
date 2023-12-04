package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.TextFormattingBar
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostSection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CreateCommentScreen(
    private val originalPost: PostModel? = null,
    private val originalComment: CommentModel? = null,
    private val editedComment: CommentModel? = null,
    private val initialText: String? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel {
            getCreateCommentViewModel(
                postId = originalPost?.id,
                parentId = originalComment?.id,
                editedCommentId = editedComment?.id,
            )
        }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        val galleryHelper = remember { getGalleryHelper() }
        var openImagePicker by remember { mutableStateOf(false) }
        var rawContent by remember { mutableStateOf<Any?>(null) }
        var textFieldValue by remember {
            mutableStateOf(
                TextFieldValue(
                    text = (initialText ?: editedComment?.text).orEmpty()
                )
            )
        }
        val commentFocusRequester = remember { FocusRequester() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    is CreateCommentMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(effect.message ?: genericError)
                    }

                    is CreateCommentMviModel.Effect.Success -> {
                        notificationCenter.send(
                            event = NotificationCenterEvent.CommentCreated,
                        )
                        if (originalPost != null) {
                            notificationCenter.send(
                                event = NotificationCenterEvent.PostUpdated(
                                    originalPost.copy(
                                        comments = if (effect.new) {
                                            originalPost.comments + 1
                                        } else {
                                            originalPost.comments
                                        }
                                    )
                                ),
                            )
                        }
                        navigationCoordinator.hideBottomSheet()
                    }

                    is CreateCommentMviModel.Effect.AddImageToText -> {
                        textFieldValue = textFieldValue.let {
                            it.copy(text = it.text + "\n![](${effect.url})")
                        }
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
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
                                    editedComment != null -> {
                                        stringResource(MR.strings.edit_comment_title)
                                    }

                                    else -> {
                                        stringResource(MR.strings.create_comment_title)
                                    }
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
                            onClick = {
                                model.reduce(
                                    CreateCommentMviModel.Intent.Send(
                                        textFieldValue.text
                                    )
                                )
                            },
                        )
                    }
                )
            },
            bottomBar = {
                Column {
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
                            model.reduce(CreateCommentMviModel.Intent.ChangeSection(section))
                        }
                    )

                    if (uiState.section == CreatePostSection.Edit) {
                        TextField(
                            modifier = Modifier
                                .focusRequester(commentFocusRequester)
                                .heightIn(min = 300.dp, max = 500.dp)
                                .fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                            label = {
                                Text(text = stringResource(MR.strings.create_comment_body))
                            },
                            textStyle = MaterialTheme.typography.bodyMedium,
                            value = textFieldValue,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                autoCorrect = true,
                            ),
                            onValueChange = { value ->
                                textFieldValue = value
                            },
                            isError = uiState.textError != null,
                            supportingText = {
                                Column {
                                    if (uiState.textError != null) {
                                        Text(
                                            text = uiState.textError?.localized().orEmpty(),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                    TextFormattingBar(
                                        textFieldValue = textFieldValue,
                                        onTextFieldValueChanged = {
                                            textFieldValue = it
                                        },
                                        onSelectImage = {
                                            openImagePicker = true
                                        }
                                    )
                                }
                            },
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .heightIn(min = 300.dp, max = 500.dp)
                                .fillMaxWidth()
                        ) {
                            PostCardBody(
                                modifier = Modifier
                                    .padding(Spacing.s)
                                    .verticalScroll(rememberScrollState()),
                                text = textFieldValue.text,
                                autoLoadImages = uiState.autoLoadImages,
                            )
                        }
                    }
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
            ) {
                val referenceModifier = Modifier.padding(
                    horizontal = Spacing.s,
                    vertical = Spacing.xxs,
                )
                when {
                    originalComment != null -> {
                        item {
                            CommentCard(
                                modifier = referenceModifier,
                                comment = originalComment,
                                hideIndent = true,
                                voteFormat = uiState.voteFormat,
                                autoLoadImages = uiState.autoLoadImages,
                                options = buildList {
                                    add(
                                        Option(
                                            OptionId.SeeRaw,
                                            stringResource(MR.strings.post_action_see_raw)
                                        )
                                    )
                                },
                                onOptionSelected = {
                                    rawContent = originalComment
                                },
                            )
                            Divider()
                        }
                    }

                    originalPost != null -> {
                        item {
                            PostCard(
                                modifier = referenceModifier,
                                postLayout = uiState.postLayout,
                                fullHeightImage = uiState.fullHeightImages,
                                post = originalPost,
                                limitBodyHeight = true,
                                blurNsfw = false,
                                voteFormat = uiState.voteFormat,
                                autoLoadImages = uiState.autoLoadImages,
                                options = buildList {
                                    add(
                                        Option(
                                            OptionId.SeeRaw,
                                            stringResource(MR.strings.post_action_see_raw)
                                        )
                                    )
                                },
                                onOptionSelected = {
                                    rawContent = originalPost
                                },
                            )
                            Divider()
                        }
                    }
                }
            }
        }

        if (uiState.loading) {
            ProgressHud()
        }

        if (openImagePicker) {
            galleryHelper.getImageFromGallery { bytes ->
                openImagePicker = false
                model.reduce(CreateCommentMviModel.Intent.ImageSelected(bytes))
            }
        }

        if (rawContent != null) {
            when (val content = rawContent) {
                is PostModel -> {
                    RawContentDialog(
                        title = content.title,
                        date = content.publishDate,
                        url = content.url,
                        text = content.text,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }

                is CommentModel -> {
                    RawContentDialog(
                        text = content.text,
                        date = content.publishDate,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }
            }
        }
    }
}
