package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.unit.login.LoginBottomSheet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ManageAccountsScreen : Screen {
    @Composable
    override fun Content() {
        val model = getScreenModel<ManageAccountsMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        var indexToDelete by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(model) {
            model.effects
                .onEach { effect ->
                    when (effect) {
                        ManageAccountsMviModel.Effect.Close -> {
                            navigationCoordinator.hideBottomSheet()
                        }
                    }
                }.launchIn(this)
        }

        Column(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(
                        top = Spacing.s,
                        bottom = Spacing.m,
                    ),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BottomSheetHeader(LocalStrings.current.manageAccountsTitle)
            LazyColumn(
                modifier = Modifier.padding(top = Spacing.m).height(250.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                itemsIndexed(uiState.accounts) { idx, account ->
                    AccountItem(
                        modifier = Modifier.fillMaxWidth(),
                        account = account,
                        autoLoadImages = uiState.autoLoadImages,
                        onClick = {
                            model.reduce(ManageAccountsMviModel.Intent.SwitchAccount(idx))
                        },
                        options =
                            buildList {
                                this +=
                                    Option(
                                        OptionId.Delete,
                                        LocalStrings.current.commentActionDelete,
                                    )
                            },
                        onOptionSelected =
                            rememberCallbackArgs(model) { optionId ->
                                when (optionId) {
                                    OptionId.Delete -> {
                                        indexToDelete = idx
                                    }

                                    else -> Unit
                                }
                            },
                    )
                }
            }

            Button(
                modifier = Modifier.padding(vertical = Spacing.m),
                onClick = {
                    navigationCoordinator.hideBottomSheet()
                    navigationCoordinator.pushScreen(LoginBottomSheet())
                },
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Text(LocalStrings.current.manageAccountsButtonAdd)
                }
            }
        }

        indexToDelete?.also { idx ->
            AlertDialog(
                onDismissRequest = {
                    indexToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            indexToDelete = null
                        },
                    ) {
                        Text(text = LocalStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(ManageAccountsMviModel.Intent.DeleteAccount(idx))
                            indexToDelete = null
                        },
                    ) {
                        Text(text = LocalStrings.current.buttonConfirm)
                    }
                },
                text = {
                    Text(text = LocalStrings.current.messageAreYouSure)
                },
            )
        }
    }
}
