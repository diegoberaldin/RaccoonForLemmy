package com.github.diegoberaldin.raccoonforlemmy.unit.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.handleUrl
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.autofill
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toReadableMessage
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.getCustomTabsHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.toUrlOpeningMode
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginBottomSheet : Screen {
    companion object {
        private const val HELP_URL = "https://join-lemmy.org/docs/users/01-getting-started.html"
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<LoginMviModel>()
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = LocalXmlStrings.current.messageGenericError
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val uriHandler = LocalUriHandler.current
        val customTabsHelper = remember { getCustomTabsHelper() }
        val settingsRepository = remember { getSettingsRepository() }
        val instanceFocusRequester = remember { FocusRequester() }
        val usernameFocusRequester = remember { FocusRequester() }
        val passwordFocusRequester = remember { FocusRequester() }
        val tokenFocusRequester = remember { FocusRequester() }

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    is LoginMviModel.Effect.LoginError -> {
                        snackbarHostState.showSnackbar(
                            message = it.message ?: genericError,
                        )
                    }

                    LoginMviModel.Effect.LoginSuccess -> {
                        navigationCoordinator.popScreen()
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = LocalXmlStrings.current.profileButtonLogin,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    navigationCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = rememberCallback {
                                navigationCoordinator.handleUrl(
                                    url = HELP_URL,
                                    openingMode = settingsRepository.currentSettings.value.urlOpeningMode.toUrlOpeningMode(),
                                    uriHandler = uriHandler,
                                    customTabsHelper = customTabsHelper,
                                    onOpenWeb = { url ->
                                        navigationCoordinator.pushScreen(WebViewScreen(url))
                                    }
                                )
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                ) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        snackbarData = data,
                    )
                }
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(Spacing.m))

                    // instance name
                    TextField(
                        modifier = Modifier.focusRequester(instanceFocusRequester),
                        label = {
                            Text(text = LocalXmlStrings.current.loginFieldInstanceName)
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
                            model.reduce(LoginMviModel.Intent.SetInstanceName(value))
                        },
                        supportingText = {
                            val error = uiState.instanceNameError
                            if (error != null) {
                                Text(
                                    text = error.toReadableMessage(),
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
                                                LoginMviModel.Intent.SetInstanceName("")
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
                        modifier = Modifier
                            .autofill(
                                autofillTypes = listOf(
                                    AutofillType.Username,
                                    AutofillType.EmailAddress,
                                ),
                                onFill = { value ->
                                    model.reduce(LoginMviModel.Intent.SetUsername(value))
                                }
                            )
                            .focusRequester(usernameFocusRequester),
                        label = {
                            Text(text = LocalXmlStrings.current.loginFieldUserName)
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
                            model.reduce(LoginMviModel.Intent.SetUsername(value))
                        },
                        supportingText = {
                            val error = uiState.usernameError
                            if (error != null) {
                                Text(
                                    text = error.toReadableMessage(),
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
                        modifier = Modifier
                            .autofill(
                                autofillTypes = listOf(AutofillType.Password),
                                onFill = { value ->
                                    model.reduce(LoginMviModel.Intent.SetPassword(value))
                                }
                            )
                            .focusRequester(passwordFocusRequester),
                        label = {
                            Text(text = LocalXmlStrings.current.loginFieldPassword)
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
                            model.reduce(LoginMviModel.Intent.SetPassword(value))
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
                            val error = uiState.passwordError
                            if (error != null) {
                                Text(
                                    text = error.toReadableMessage(),
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
                                Text(text = LocalXmlStrings.current.loginFieldToken)
                                Text(
                                    text = LocalXmlStrings.current.loginFieldLabelOptional,
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
                            model.reduce(LoginMviModel.Intent.SetTotp2faToken(value))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                    )

                    Spacer(modifier = Modifier.height(Spacing.m))

                    Button(
                        modifier = Modifier.padding(top = Spacing.l),
                        onClick = rememberCallback(model) {
                            model.reduce(LoginMviModel.Intent.Confirm)
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
                            Text(LocalXmlStrings.current.buttonConfirm)
                        }
                    }
                }
            }
        )
    }
}
