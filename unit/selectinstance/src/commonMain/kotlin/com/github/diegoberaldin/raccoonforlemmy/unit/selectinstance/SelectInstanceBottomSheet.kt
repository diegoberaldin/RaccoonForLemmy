package com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.components.SelectInstanceItem
import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.dialog.ChangeInstanceDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class SelectInstanceBottomSheet : Screen {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<SelectInstanceMviModel>(key)
        val uiState by model.uiState.collectAsState()
        var changeInstanceDialogOpen by remember {
            mutableStateOf(false)
        }
        val notificationCenter = remember { getNotificationCenter() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val lazyListState = rememberLazyListState()
        val reorderableLazyColumnState =
            rememberReorderableLazyListState(lazyListState) { from, to ->
                model.reduce(
                    SelectInstanceMviModel.Intent.SwapIntances(
                        from = from.index - 1,
                        to = to.index - 1,
                    ),
                )
            }
        var instanceToDelete by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(model) {
            model.effects.onEach { evt ->
                when (evt) {
                    SelectInstanceMviModel.Effect.CloseDialog -> {
                        changeInstanceDialogOpen = false
                    }

                    is SelectInstanceMviModel.Effect.Confirm -> {
                        notificationCenter.send(NotificationCenterEvent.InstanceSelected(evt.instance))
                        navigationCoordinator.hideBottomSheet()
                    }
                }
            }.launchIn(this)
        }

        Column(
            modifier =
                Modifier.padding(
                    top = Spacing.s,
                    start = Spacing.s,
                    end = Spacing.s,
                    bottom = Spacing.m,
                ).fillMaxHeight(0.6f),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
            ) {
                BottomSheetHeader(LocalXmlStrings.current.dialogTitleChangeInstance)
                IconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    content = {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    onClick = {
                        changeInstanceDialogOpen = true
                    },
                )
            }
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                if (uiState.instances.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                            textAlign = TextAlign.Center,
                            text = LocalXmlStrings.current.messageEmptyList,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                } else {
                    item {
                        // workaround for https://github.com/Calvin-LL/Reorderable/issues/4
                        ReorderableItem(
                            state = reorderableLazyColumnState,
                            key = "dummy",
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(Dp.Hairline),
                        ) {}
                    }
                }
                items(
                    items = uiState.instances,
                    key = { it },
                ) { instance ->
                    ReorderableItem(
                        state = reorderableLazyColumnState,
                        key = instance,
                    ) { isDragging ->
                        val isActive = instance == uiState.currentInstance
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                        Surface(
                            shadowElevation = elevation,
                        ) {
                            SelectInstanceItem(
                                modifier = Modifier.draggableHandle(),
                                instance = instance,
                                isActive = isActive,
                                onClick =
                                    rememberCallback(model) {
                                        model.reduce(
                                            SelectInstanceMviModel.Intent.SelectInstance(instance),
                                        )
                                    },
                                options =
                                    buildList {
                                        if (!isActive) {
                                            this +=
                                                Option(
                                                    OptionId.Delete,
                                                    LocalXmlStrings.current.commentActionDelete,
                                                )
                                        }
                                    },
                                onOptionSelected =
                                    rememberCallbackArgs { optionId ->
                                        when (optionId) {
                                            OptionId.Delete -> {
                                                instanceToDelete = instance
                                            }

                                            else -> Unit
                                        }
                                    },
                            )
                        }
                    }
                }
            }
        }

        if (changeInstanceDialogOpen) {
            ChangeInstanceDialog(
                instanceName = uiState.changeInstanceName,
                instanceNameError = uiState.changeInstanceNameError,
                loading = uiState.changeInstanceLoading,
                onClose = {
                    changeInstanceDialogOpen = false
                },
                onChangeInstanceName = { value ->
                    model.reduce(SelectInstanceMviModel.Intent.ChangeInstanceName(value))
                },
                onSubmit = {
                    model.reduce(SelectInstanceMviModel.Intent.SubmitChangeInstanceDialog)
                },
            )
        }

        instanceToDelete?.also { instance ->
            AlertDialog(
                onDismissRequest = {
                    instanceToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            instanceToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(SelectInstanceMviModel.Intent.DeleteInstance(instance))
                            instanceToDelete = null
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
