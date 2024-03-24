package com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.unit.login.LoginBottomSheet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ManageAccountsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ManageAccountsMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        var indexToDelete by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    ManageAccountsMviModel.Effect.Close -> {
                        navigationCoordinator.hideBottomSheet()
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BottomSheetHandle()
                            Text(
                                modifier = Modifier.padding(top = Spacing.s),
                                text = LocalXmlStrings.current.manageAccountsTitle,
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier.padding(paddingValues).fillMaxWidth(),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(vertical = Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    itemsIndexed(uiState.accounts) { idx, account ->
                        AccountItem(
                            account = account,
                            autoLoadImages = uiState.autoLoadImages,
                            onClick = rememberCallback {
                                model.reduce(ManageAccountsMviModel.Intent.SwitchAccount(idx))
                            },
                            options = buildList {
                                this += Option(
                                    OptionId.Delete,
                                    LocalXmlStrings.current.commentActionDelete,
                                )
                            },
                            onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                when (optionId) {
                                    OptionId.Delete -> {
                                        indexToDelete = idx
                                    }

                                    else -> Unit
                                }
                            },
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(Spacing.m))
                        Button(
                            onClick = {
                                navigationCoordinator.showBottomSheet(LoginBottomSheet())
                            },
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Text(LocalXmlStrings.current.manageAccountsButtonAdd)
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.interItem))
                    }
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
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(ManageAccountsMviModel.Intent.DeleteAccount(idx))
                            indexToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonConfirm)
                    }
                },
                text = {
                    Text(text = LocalXmlStrings.current.messageAreYouSure)
                },
            )
        }
    }
}
