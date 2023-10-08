package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostSection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
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
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val notificationCenter = remember { getNotificationCenter() }

        LaunchedEffect(model) {
            if (editedComment != null) {
                model.reduce(CreateCommentMviModel.Intent.SetText(editedComment.text))
            }

            model.effects.onEach {
                when (it) {
                    is CreateCommentMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(it.message ?: genericError)
                    }

                    CreateCommentMviModel.Effect.Success -> {
                        notificationCenter.getObserver(NotificationCenterContractKeys.CommentCreated)
                            ?.also { o -> o.invoke(Unit) }
                        bottomSheetNavigator.hide()
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                            verticalArrangement = Arrangement.spacedBy(Spacing.s),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BottomSheetHandle()
                            Text(
                                text = stringResource(MR.strings.create_comment_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    },
                )
            },
        ) { padding ->
            val focusManager = LocalFocusManager.current
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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .nestedScroll(keyboardScrollConnection)
                    .verticalScroll(rememberScrollState()),
            ) {
                val themeRepository = remember { getThemeRepository() }
                val fontScale by themeRepository.contentFontScale.collectAsState()
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = LocalDensity.current.density,
                        fontScale = fontScale,
                    ),
                ) {
                    val referenceModifier = Modifier.padding(
                        horizontal = Spacing.s,
                        vertical = Spacing.xxs,
                    )
                    when {
                        originalComment != null -> {
                            CommentCard(
                                modifier = referenceModifier,
                                comment = originalComment,
                                hideIndent = true,
                                separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                autoLoadImages = uiState.autoLoadImages,
                            )
                            Divider()
                        }

                        originalPost != null -> {
                            PostCard(
                                modifier = referenceModifier,
                                postLayout = uiState.postLayout,
                                post = originalPost,
                                blurNsfw = false,
                                separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                autoLoadImages = uiState.autoLoadImages,
                            )
                            Divider()
                        }
                    }
                }

                Box(
                    modifier = Modifier.padding(vertical = Spacing.s).fillMaxWidth().height(1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(1.dp),
                        ),
                )

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
                    val commentFocusRequester = remember { FocusRequester() }
                    TextField(
                        modifier = Modifier.focusRequester(commentFocusRequester)
                            .heightIn(min = 300.dp, max = 500.dp).fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        label = {
                            Text(text = stringResource(MR.strings.create_comment_body))
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        value = uiState.text,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            autoCorrect = true,
                        ),
                        onValueChange = { value ->
                            model.reduce(CreateCommentMviModel.Intent.SetText(value))
                        },
                        isError = uiState.textError != null,
                        supportingText = {
                            if (uiState.textError != null) {
                                Text(
                                    text = uiState.textError?.localized().orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        trailingIcon = {
                            IconButton(
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    model.reduce(CreateCommentMviModel.Intent.Send)
                                },
                            )
                        }
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
                            text = uiState.text,
                            autoLoadImages = uiState.autoLoadImages,
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.xxl))
            }
        }

        if (uiState.loading) {
            ProgressHud()
        }
    }
}