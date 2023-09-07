package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CreateCommentScreen(
    private val originalPost: PostModel,
    private val originalComment: CommentModel? = null,
    private val onCommentCreated: () -> Unit = {},
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel {
            getCreateCommentViewModel(postId = originalPost.id, parentId = originalComment?.id)
        }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    is CreateCommentMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(it.message ?: genericError)
                    }

                    CreateCommentMviModel.Effect.Success -> onCommentCreated()
                }
            }.launchIn(this)
        }

        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.width(60.dp)
                            .height(1.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(1.dp),
                            ),
                    )
                    Text(
                        text = stringResource(MR.strings.create_comment_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding)
                    .verticalScroll(rememberScrollState())
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
                    if (originalComment != null) {
                        CommentCard(
                            modifier = referenceModifier,
                            comment = originalComment
                        )
                    } else {
                        PostCard(
                            modifier = referenceModifier,
                            post = originalPost,
                            blurNsfw = false
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(vertical = Spacing.s)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(1.dp),
                        ),
                )

                TextField(
                    modifier = Modifier
                        .heightIn(min = 300.dp, max = 500.dp)
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                    label = {
                        Text(text = stringResource(MR.strings.create_comment_body))
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = uiState.text,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrect = false,
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            model.reduce(CreateCommentMviModel.Intent.Send)
                        }
                    ),
                    onValueChange = { value ->
                        model.reduce(CreateCommentMviModel.Intent.SetText(value))
                    },
                )

                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        content = {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            model.reduce(CreateCommentMviModel.Intent.Send)
                        }
                    )
                }
            }
        }
    }
}