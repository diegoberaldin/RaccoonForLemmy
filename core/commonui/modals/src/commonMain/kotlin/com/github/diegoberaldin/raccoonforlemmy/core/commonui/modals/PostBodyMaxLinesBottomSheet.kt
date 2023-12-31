package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class PostBodyMaxLinesBottomSheet(
    private val values: List<Int?> = listOf(
        10,
        30,
        50,
        -1,// custom number
        null,// unlimited
    ),
) : Screen {

    @Composable
    override fun Content() {
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        var customDialogOpened by remember { mutableStateOf(false) }
        val settingsRepository = remember { getSettingsRepository() }

        Column(
            modifier = Modifier.padding(
                top = Spacing.s,
                start = Spacing.s,
                end = Spacing.s,
                bottom = Spacing.m,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BottomSheetHandle()
                Text(
                    modifier = Modifier.padding(
                        start = Spacing.s,
                        top = Spacing.s,
                        end = Spacing.s,
                    ),
                    text = stringResource(MR.strings.settings_zombie_mode_interval),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
            ) {
                for (value in values) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = Spacing.s,
                            vertical = Spacing.s,
                        ).fillMaxWidth().onClick(
                            onClick = rememberCallback {
                                if (value != null && value < 0) {
                                    customDialogOpened = true
                                } else {
                                    notificationCenter.send(
                                        NotificationCenterEvent.ChangePostBodyMaxLines(value)
                                    )
                                    navigationCoordinator.hideBottomSheet()
                                }
                            },
                        ),
                    ) {
                        val text = when {
                            value == null -> stringResource(MR.strings.settings_post_body_max_lines_unlimited)
                            value < 0 -> stringResource(MR.strings.settings_color_custom)
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
            val current = settingsRepository.currentSettings.value.postBodyMaxLines ?: 0
            NumberPickerDialog(
                title = stringResource(MR.strings.settings_color_custom),
                initialValue = current,
                onClose = {
                    customDialogOpened = false
                },
                onSubmit = { value ->
                    notificationCenter.send(NotificationCenterEvent.ChangePostBodyMaxLines(value))
                    navigationCoordinator.hideBottomSheet()
                }
            )
        }
    }
}
