package com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.components.SelectInstanceItem
import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.dialog.ChangeInstanceDialog
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SelectInstanceBottomSheet : Screen {
    @Composable
    override fun Content() {
        val model = getScreenModel<SelectInstanceMviModel>(key)
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        var changeInstanceDialogOpen by remember {
            mutableStateOf(false)
        }
        val notificationCenter = remember { getNotificationCenter() }
        val navigationCoordinator = remember { getNavigationCoordinator() }

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
            modifier = Modifier.padding(
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
                Column(
                    modifier = Modifier.align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BottomSheetHandle()
                    Text(
                        modifier = Modifier.padding(
                            start = Spacing.s,
                            top = Spacing.s,
                            end = Spacing.s,
                        ),
                        text = stringResource(MR.strings.dialog_title_change_instance),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
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
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
            ) {
                if (uiState.instances.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                            textAlign = TextAlign.Center,
                            text = stringResource(MR.strings.message_empty_list),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                items(
                    items = uiState.instances,
                    key = { it },
                ) { instance ->
                    val isActive = instance == uiState.currentInstance
                    SelectInstanceItem(
                        modifier = Modifier.fillMaxWidth(),
                        instance = instance,
                        isActive = isActive,
                        onClick = rememberCallback(model) {
                            model.reduce(SelectInstanceMviModel.Intent.SelectInstance(instance))
                        },
                        options = buildList {
                            if (!isActive) {
                                this += Option(
                                    OptionId.Delete,
                                    stringResource(MR.strings.comment_action_delete),
                                )
                            }
                        },
                        onOptionSelected = rememberCallbackArgs { optionId ->
                            when (optionId) {
                                OptionId.Delete -> {
                                    model.reduce(
                                        SelectInstanceMviModel.Intent.DeleteInstance(
                                            instance,
                                        ),
                                    )
                                }

                                else -> Unit
                            }
                        }
                    )
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
    }
}
