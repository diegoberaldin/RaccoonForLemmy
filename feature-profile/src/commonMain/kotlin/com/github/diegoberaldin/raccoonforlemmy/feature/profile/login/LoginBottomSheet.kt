package com.github.diegoberaldin.raccoonforlemmy.feature.profile.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.getLoginBottomSheetViewModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginBottomSheet(
    private val onHide: () -> Unit,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getLoginBottomSheetViewModel() }
        model.bindToLifecycle(key)

        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    is LoginBottomSheetMviModel.Effect.LoginError -> {
                        snackbarHostState.showSnackbar(
                            message = it.message ?: genericError,
                        )
                    }

                    LoginBottomSheetMviModel.Effect.LoginSuccess -> {
                        snackbarHostState.showSnackbar(
                            message = "Successfully logged in! \uD83C\uDF89\uD83C\uDF89\uD83C\uDF89",
                        )
                        onHide()
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .fillMaxHeight(0.65f)
                .background(MaterialTheme.colorScheme.surface),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.padding(start = Spacing.s, top = Spacing.s),
                            text = stringResource(MR.strings.profile_button_login),
                        )
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },
        ) {
            Column(
                modifier = Modifier.padding(it).fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val instanceFocusRequester = remember { FocusRequester() }
                val usernameFocusRequester = remember { FocusRequester() }
                val passwordFocusRequester = remember { FocusRequester() }
                val tokenFocusRequester = remember { FocusRequester() }

                TextField(
                    modifier = Modifier.focusRequester(instanceFocusRequester),
                    label = {
                        Text(text = stringResource(MR.strings.login_field_instance_name))
                    },
                    singleLine = true,
                    value = uiState.instanceName,
                    isError = uiState.instanceNameError != null,
                    keyboardActions = KeyboardActions(
                        onNext = {
                            usernameFocusRequester.requestFocus()
                        },
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrect = false,
                        imeAction = ImeAction.Next,
                    ),
                    onValueChange = { value ->
                        model.reduce(LoginBottomSheetMviModel.Intent.SetInstanceName(value))
                    },
                )
                if (uiState.instanceNameError != null) {
                    Text(
                        text = uiState.instanceNameError?.localized().orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                TextField(
                    modifier = Modifier.focusRequester(usernameFocusRequester),
                    label = {
                        Text(text = stringResource(MR.strings.login_field_user_name))
                    },
                    singleLine = true,
                    value = uiState.username,
                    isError = uiState.usernameError != null,
                    keyboardActions = KeyboardActions(
                        onNext = {
                            passwordFocusRequester.requestFocus()
                        },
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrect = false,
                        imeAction = ImeAction.Next,
                    ),
                    onValueChange = { value ->
                        model.reduce(LoginBottomSheetMviModel.Intent.SetUsername(value))
                    },
                )
                if (uiState.usernameError != null) {
                    Text(
                        text = uiState.usernameError?.localized().orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                var transformation: VisualTransformation by remember {
                    mutableStateOf(PasswordVisualTransformation())
                }
                TextField(
                    modifier = Modifier.focusRequester(passwordFocusRequester),
                    label = {
                        Text(text = stringResource(MR.strings.login_field_password))
                    },
                    singleLine = true,
                    value = uiState.password,
                    isError = uiState.passwordError != null,
                    keyboardActions = KeyboardActions(
                        onNext = {
                            tokenFocusRequester.requestFocus()
                        },
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                    onValueChange = { value ->
                        model.reduce(LoginBottomSheetMviModel.Intent.SetPassword(value))
                    },
                    visualTransformation = transformation,
                    trailingIcon = {
                        Image(
                            modifier = Modifier.onClick {
                                transformation = if (transformation == VisualTransformation.None) {
                                    PasswordVisualTransformation()
                                } else {
                                    VisualTransformation.None
                                }
                            },
                            imageVector = if (transformation == VisualTransformation.None) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface),
                        )
                    },
                )
                if (uiState.passwordError != null) {
                    Text(
                        text = uiState.passwordError?.localized().orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                TextField(
                    modifier = Modifier.focusRequester(tokenFocusRequester),
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(text = stringResource(MR.strings.login_field_token))
                            Text(
                                text = stringResource(MR.strings.login_field_label_optional),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    },
                    singleLine = true,
                    value = uiState.totp2faToken,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    onValueChange = { value ->
                        model.reduce(LoginBottomSheetMviModel.Intent.SetTotp2faToken(value))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                )
                Spacer(modifier = Modifier.height(Spacing.m))
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        model.reduce(LoginBottomSheetMviModel.Intent.Confirm)
                    },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (uiState.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Text(stringResource(MR.strings.button_confirm))
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.m))
            }
        }
    }
}
