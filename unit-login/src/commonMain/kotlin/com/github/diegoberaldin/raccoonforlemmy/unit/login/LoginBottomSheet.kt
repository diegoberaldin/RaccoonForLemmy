package com.github.diegoberaldin.raccoonforlemmy.unit.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.handleUrl
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.login.di.getLoginBottomSheetViewModel
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginBottomSheet : Screen {
    companion object {
        private const val HELP_URL = "https://join-lemmy.org/docs/users/01-getting-started.html"
    }

    @Composable
    override fun Content() {
        val model = rememberScreenModel { getLoginBottomSheetViewModel() }
        model.bindToLifecycle(key)

        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)
        val navigationCoordinator = remember { getNavigationCoordinator() }

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    is LoginBottomSheetMviModel.Effect.LoginError -> {
                        snackbarHostState.showSnackbar(
                            message = it.message ?: genericError,
                        )
                    }

                    LoginBottomSheetMviModel.Effect.LoginSuccess -> {
                        navigationCoordinator.hideBottomSheet()
                    }
                }
            }.launchIn(this)
        }

        val uriHandler = LocalUriHandler.current
        val settingsRepository = remember { getSettingsRepository() }

        Box(
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier.padding(
                    top = Spacing.s,
                    start = Spacing.s,
                    end = Spacing.s,
                    bottom = Spacing.m,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BottomSheetHandle()
                        Text(
                            modifier = Modifier.padding(start = Spacing.s, top = Spacing.s),
                            text = stringResource(MR.strings.profile_button_login),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    IconButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = rememberCallback {
                            navigationCoordinator.hideBottomSheet()
                            navigationCoordinator.handleUrl(
                                url = HELP_URL,
                                openExternal = settingsRepository.currentSettings.value.openUrlsInExternalBrowser,
                                uriHandler = uriHandler,
                                onOpenWeb = { url ->
                                    navigationCoordinator.pushScreen(WebViewScreen(url))
                                }
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                val instanceFocusRequester = remember { FocusRequester() }
                val usernameFocusRequester = remember { FocusRequester() }
                val passwordFocusRequester = remember { FocusRequester() }
                val tokenFocusRequester = remember { FocusRequester() }

                // instance name
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
                    onValueChange = rememberCallbackArgs(model) { value ->
                        model.reduce(LoginBottomSheetMviModel.Intent.SetInstanceName(value))
                    },
                    supportingText = {
                        if (uiState.instanceNameError != null) {
                            Text(
                                text = uiState.instanceNameError?.localized().orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    trailingIcon = {
                        if (uiState.instanceName.isNotEmpty()) {
                            Icon(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback(model) {
                                        model.reduce(
                                            LoginBottomSheetMviModel.Intent.SetInstanceName("")
                                        )
                                    },
                                ),
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                            )
                        }
                    },
                )

                // user name
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
                    onValueChange = rememberCallbackArgs(model) { value ->
                        model.reduce(LoginBottomSheetMviModel.Intent.SetUsername(value))
                    },
                    supportingText = {
                        if (uiState.usernameError != null) {
                            Text(
                                text = uiState.usernameError?.localized().orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                // password
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
                    onValueChange = rememberCallbackArgs(model) { value ->
                        model.reduce(LoginBottomSheetMviModel.Intent.SetPassword(value))
                    },
                    visualTransformation = transformation,
                    trailingIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    transformation =
                                        if (transformation == VisualTransformation.None) {
                                            PasswordVisualTransformation()
                                        } else {
                                            VisualTransformation.None
                                        }
                                },
                            ),
                            imageVector = if (transformation == VisualTransformation.None) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    supportingText = {
                        if (uiState.passwordError != null) {
                            Text(
                                text = uiState.passwordError?.localized().orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                // TOTP 2FA token
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
                    onValueChange = rememberCallbackArgs(model) { value ->
                        model.reduce(LoginBottomSheetMviModel.Intent.SetTotp2faToken(value))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                )
                Spacer(modifier = Modifier.height(Spacing.m))
                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = rememberCallback(model) {
                        model.reduce(LoginBottomSheetMviModel.Intent.Confirm)
                    },
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (uiState.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(IconSize.s),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Text(stringResource(MR.strings.button_confirm))
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.m))
            }

            SnackbarHost(
                modifier = Modifier.padding(bottom = Spacing.xxxl),
                hostState = snackbarHostState
            )
        }
    }
}
