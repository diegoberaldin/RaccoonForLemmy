package com.github.diegoberaldin.raccoonforlemmy.unit.ban

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsIntValueRow
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsSwitchRow
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toReadableMessage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

class BanUserScreen(
    private val userId: Int,
    private val communityId: Int,
    private val newValue: Boolean,
    private val postId: Int? = null,
    private val commentId: Int? = null,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<BanUserMviModel> {
            parametersOf(
                userId,
                communityId,
                newValue,
                postId,
                commentId,
            )
        }
        val uiState by model.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = LocalXmlStrings.current.messageGenericError
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    is BanUserMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(it.message ?: genericError)
                    }

                    BanUserMviModel.Effect.Success -> {
                        navigationCoordinator.hideBottomSheet()
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
                        val title = if (newValue) {
                            LocalXmlStrings.current.modActionBan
                        } else {
                            LocalXmlStrings.current.modActionAllow
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    actions = {
                        IconButton(
                            content = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                )
                            },
                            onClick = rememberCallback(model) {
                                model.reduce(BanUserMviModel.Intent.Submit)
                            },
                        )
                    },
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        snackbarData = data,
                    )
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val focusRequester = remember { FocusRequester() }
                TextField(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .heightIn(min = 300.dp, max = 500.dp)
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    label = {
                        Text(text = LocalXmlStrings.current.banReasonPlaceholder)
                    },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = uiState.text,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        autoCorrect = true,
                    ),
                    onValueChange = { value ->
                        model.reduce(BanUserMviModel.Intent.SetText(value))
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

                if (uiState.targetBanValue) {
                    // it is a ban (as opposed to unban)
                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.banItemPermanent,
                        value = uiState.permanent,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(BanUserMviModel.Intent.ChangePermanent(value))
                        },
                    )

                    if (!uiState.permanent) {
                        SettingsIntValueRow(
                            title = LocalXmlStrings.current.banItemDurationDays,
                            value = uiState.days,
                            onIncrement = rememberCallback {
                                model.reduce(BanUserMviModel.Intent.IncrementDays)
                            },
                            onDecrement = rememberCallback {
                                model.reduce(BanUserMviModel.Intent.DecrementDays)
                            },
                        )
                    }

                    SettingsSwitchRow(
                        title = LocalXmlStrings.current.banItemRemoveData,
                        value = uiState.removeData,
                        onValueChanged = rememberCallbackArgs(model) { value ->
                            model.reduce(BanUserMviModel.Intent.ChangeRemoveData(value))
                        },
                    )
                }

                Spacer(Modifier.height(Spacing.xxl))
            }

            if (uiState.loading) {
                ProgressHud()
            }

            SnackbarHost(
                modifier = Modifier.padding(bottom = Spacing.xxxl),
                hostState = snackbarHostState
            ) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    snackbarData = data,
                )
            }
        }
    }
}