package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHeader
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick

sealed interface SelectNumberBottomSheetType {
    data object PostBodyMaxLines : SelectNumberBottomSheetType

    data object InboxPreviewMaxLines : SelectNumberBottomSheetType
}

@Composable
private fun SelectNumberBottomSheetType.toReadableTitle() =
    when (this) {
        SelectNumberBottomSheetType.InboxPreviewMaxLines -> LocalStrings.current.settingsInboxPreviewMaxLines
        SelectNumberBottomSheetType.PostBodyMaxLines -> LocalStrings.current.settingsPostBodyMaxLines
    }

private fun SelectNumberBottomSheetType.toInt() =
    when (this) {
        SelectNumberBottomSheetType.InboxPreviewMaxLines -> 1
        SelectNumberBottomSheetType.PostBodyMaxLines -> 0
    }

fun Int.toSelectNumberBottomSheetType(): SelectNumberBottomSheetType =
    when (this) {
        1 -> SelectNumberBottomSheetType.InboxPreviewMaxLines
        else -> SelectNumberBottomSheetType.PostBodyMaxLines
    }

class SelectNumberBottomSheet(
    private val values: List<Int?> =
        listOf(
            10,
            30,
            50,
            -1, // custom number
            null, // unlimited
        ),
    private val initialValue: Int?,
    private val type: SelectNumberBottomSheetType,
) : Screen {
    @Composable
    override fun Content() {
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        var customDialogOpened by remember { mutableStateOf(false) }

        Column(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(
                        top = Spacing.s,
                        start = Spacing.s,
                        end = Spacing.s,
                        bottom = Spacing.m,
                    ),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            BottomSheetHeader(title = type.toReadableTitle())
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                for (value in values) {
                    Row(
                        modifier =
                            Modifier
                                .padding(
                                    horizontal = Spacing.s,
                                    vertical = Spacing.s,
                                ).fillMaxWidth()
                                .onClick(
                                    onClick = {
                                        if (value != null && value < 0) {
                                            customDialogOpened = true
                                        } else {
                                            notificationCenter.send(
                                                NotificationCenterEvent.SelectNumberBottomSheetClosed(
                                                    value = value,
                                                    type = type.toInt(),
                                                ),
                                            )
                                            navigationCoordinator.hideBottomSheet()
                                        }
                                    },
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val text =
                            when {
                                value == null -> LocalStrings.current.settingsPostBodyMaxLinesUnlimited
                                value < 0 -> LocalStrings.current.settingsColorCustom
                                else -> value.toString()
                            }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }

        if (customDialogOpened) {
            NumberPickerDialog(
                title = LocalStrings.current.settingsColorCustom,
                initialValue = initialValue ?: 0,
                onClose = {
                    customDialogOpened = false
                },
                onSubmit = { value ->
                    notificationCenter.send(
                        NotificationCenterEvent.SelectNumberBottomSheetClosed(
                            value = value,
                            type = type.toInt(),
                        ),
                    )
                    navigationCoordinator.hideBottomSheet()
                },
            )
        }
    }
}
