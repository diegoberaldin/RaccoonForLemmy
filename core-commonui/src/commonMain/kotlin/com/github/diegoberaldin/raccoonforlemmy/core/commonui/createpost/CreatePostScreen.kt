package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getCreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CreatePostScreen(
    private val communityId: Int,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getCreatePostViewModel(communityId) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val notificationCenter = remember { getNotificationCenter() }

        LaunchedEffect(model) {
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
                                text = stringResource(MR.strings.create_post_title),
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
                                model.reduce(CreatePostMviModel.Intent.Send)
                            }
                        )
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                val bodyFocusRequester = remember { FocusRequester() }
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
                    keyboardActions = KeyboardActions(
                        onNext = {
                            bodyFocusRequester.requestFocus()
                        }
                    ),
                    onValueChange = { value ->
                        model.reduce(CreatePostMviModel.Intent.SetTitle(value))
                    },
                )

                TextField(
                    modifier = Modifier.height(500.dp)
                        .fillMaxWidth()
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
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    onValueChange = { value ->
                        model.reduce(CreatePostMviModel.Intent.SetText(value))
                    },
                )

                Spacer(Modifier.height(Spacing.xxl))
            }
        }
    }
}