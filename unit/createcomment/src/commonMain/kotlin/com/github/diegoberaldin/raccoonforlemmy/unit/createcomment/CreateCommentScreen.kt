package com.github.diegoberaldin.raccoonforlemmy.unit.createcomment

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.toTypography
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CreatePostSection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.TextFormattingBar
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SelectLanguageDialog
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.getGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toReadableMessage
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

class CreateCommentScreen(
    private val draftId: Long? = null,
    private val originalPostId: Long? = null,
    private val originalCommentId: Long? = null,
    private val editedCommentId: Long? = null,
    private val initialText: String? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<CreateCommentMviModel> {
            parametersOf(
                originalPostId,
                originalCommentId,
                editedCommentId,
                draftId,
            )
        }
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = LocalXmlStrings.current.messageGenericError
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        val galleryHelper = remember { getGalleryHelper() }
        var openImagePicker by remember { mutableStateOf(false) }
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val commentFocusRequester = remember { FocusRequester() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
        val themeRepository = remember { getThemeRepository() }
        val contentFontFamily by themeRepository.contentFontFamily.collectAsState()
        val typography = contentFontFamily.toTypography()
        var selectLanguageDialogOpen by remember { mutableStateOf(false) }

        LaunchedEffect(uiState.editedComment) {
            uiState.editedComment?.also { editedComment ->
                model.reduce(CreateCommentMviModel.Intent.ChangeLanguage(editedComment.languageId))
                val newValue = TextFieldValue(text = editedComment.text)
                model.reduce(CreateCommentMviModel.Intent.ChangeTextValue(newValue))
            }
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    is CreateCommentMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(effect.message ?: genericError)
                    }

                    is CreateCommentMviModel.Effect.Success -> {
                        notificationCenter.send(event = NotificationCenterEvent.CommentCreated)
                        uiState.originalPost?.also { originalPost ->
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
                        navigationCoordinator.popScreen()
                    }

                    CreateCommentMviModel.Effect.DraftSaved -> navigationCoordinator.popScreen()
                }
            }.launchIn(this)
        }
        LaunchedEffect(initialText) {
            if (uiState.textValue.text.isEmpty() && !initialText.isNullOrEmpty()) {
                model.reduce(CreateCommentMviModel.Intent.ChangeTextValue(TextFieldValue(initialText)))
            }
        }

        Scaffold(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding(),
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
                        Text(
                            text = when {
                                uiState.editedComment != null -> {
                                    LocalXmlStrings.current.editCommentTitle
                                }

                                else -> {
                                    LocalXmlStrings.current.createCommentTitle
                                }
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    actions = {
                        if (uiState.editedComment == null) {
                            IconButton(
                                modifier = Modifier.padding(horizontal = Spacing.xs),
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                    )
                                },
                                onClick = rememberCallback(model) {
                                    model.reduce(CreateCommentMviModel.Intent.SaveDraft)
                                },
                            )
                        }
                        IconButton(
                            modifier = Modifier.padding(horizontal = Spacing.xs),
                            content = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.Send,
                                    contentDescription = null,
                                )
                            },
                            onClick = rememberCallback(model) {
                                model.reduce(CreateCommentMviModel.Intent.Send)
                            },
                        )
                    }
                )
            },
            bottomBar = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    SectionSelector(
                        titles = listOf(
                            LocalXmlStrings.current.createPostTabEditor,
                            LocalXmlStrings.current.createPostTabPreview,
                        ),
                        currentSection = when (uiState.section) {
                            CreatePostSection.Preview -> 1
                            else -> 0
                        },
                        onSectionSelected = rememberCallbackArgs { id ->
                            val section = when (id) {
                                1 -> CreatePostSection.Preview
                                else -> CreatePostSection.Edit
                            }
                            model.reduce(CreateCommentMviModel.Intent.ChangeSection(section))
                        }
                    )

                    if (uiState.section == CreatePostSection.Edit) {
                        TextFormattingBar(
                            modifier = Modifier.padding(
                                top = Spacing.s,
                                start = Spacing.m,
                                end = Spacing.m,
                            ),
                            textFieldValue = uiState.textValue,
                            onTextFieldValueChanged = { value ->
                                model.reduce(CreateCommentMviModel.Intent.ChangeTextValue(value))
                            },
                            onSelectImage = {
                                openImagePicker = true
                            },
                            currentLanguageId = uiState.currentLanguageId,
                            availableLanguages = uiState.availableLanguages,
                            onSelectLanguage = {
                                selectLanguageDialogOpen = true
                            },
                        )
                        TextField(
                            modifier = Modifier
                                .focusRequester(commentFocusRequester)
                                .heightIn(min = 300.dp, max = 400.dp)
                                .fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                            label = {
                                Text(
                                    text = LocalXmlStrings.current.createCommentBody,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            },
                            textStyle = typography.bodyMedium,
                            value = uiState.textValue,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                autoCorrect = true,
                                capitalization = KeyboardCapitalization.Sentences,
                            ),
                            onValueChange = { value ->
                                model.reduce(CreateCommentMviModel.Intent.ChangeTextValue(value))
                            },
                            isError = uiState.textError != null,
                            supportingText = {
                                val error = uiState.textError
                                if (error != null) {
                                    Text(
                                        text = error.toReadableMessage(),
                                        color = MaterialTheme.colorScheme.error,
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
                                text = uiState.textValue.text,
                                autoLoadImages = uiState.autoLoadImages,
                            )
                        }
                    }

                    if (uiState.currentUser.isNotEmpty()) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.m),
                            text = buildString {
                                append(LocalXmlStrings.current.postReplySourceAccount)
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
                            textAlign = TextAlign.End,
                        )
                    }
                }
            },
        ) { padding ->
            val referenceModifier = Modifier.padding(
                horizontal = Spacing.s,
                vertical = Spacing.xxs,
            )
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
            ) {
                item {
                    val originalComment = uiState.originalComment
                    val originalPost = uiState.originalPost
                    if (originalComment != null) {
                        CommentCard(
                            modifier = referenceModifier,
                            comment = originalComment,
                            preferNicknames = uiState.preferNicknames,
                            hideIndent = true,
                            voteFormat = uiState.voteFormat,
                            autoLoadImages = uiState.autoLoadImages,
                            showScores = uiState.showScores,
                            options = buildList {
                                add(
                                    Option(
                                        OptionId.SeeRaw,
                                        LocalXmlStrings.current.postActionSeeRaw
                                    )
                                )
                            },
                            onOptionSelected = {
                                rawContent = originalComment
                            },
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                    } else if (originalPost != null) {
                        PostCard(
                            modifier = referenceModifier,
                            postLayout = if (uiState.postLayout == PostLayout.Card) {
                                uiState.postLayout
                            } else {
                                PostLayout.Full
                            },
                            fullHeightImage = uiState.fullHeightImages,
                            fullWidthImage = uiState.fullWidthImages,
                            post = originalPost,
                            blurNsfw = false,
                            includeFullBody = true,
                            voteFormat = uiState.voteFormat,
                            autoLoadImages = uiState.autoLoadImages,
                            preferNicknames = uiState.preferNicknames,
                            showScores = uiState.showScores,
                            options = buildList {
                                add(
                                    Option(
                                        OptionId.SeeRaw,
                                        LocalXmlStrings.current.postActionSeeRaw
                                    )
                                )
                            },
                            onOptionSelected = {
                                rawContent = originalPost
                            },
                        )
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
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        url = content.url,
                        text = content.text,
                        upVotes = content.upvotes,
                        downVotes = content.downvotes,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }

                is CommentModel -> {
                    RawContentDialog(
                        text = content.text,
                        upVotes = content.upvotes,
                        downVotes = content.downvotes,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }
            }
        }

        if (selectLanguageDialogOpen) {
            SelectLanguageDialog(
                languages = uiState.availableLanguages,
                currentLanguageId = uiState.currentLanguageId,
                onSelect = rememberCallbackArgs { langId ->
                    model.reduce(CreateCommentMviModel.Intent.ChangeLanguage(langId))
                    selectLanguageDialogOpen = false
                },
                onDismiss = rememberCallback {
                    selectLanguageDialogOpen = false
                }
            )
        }
    }
}
