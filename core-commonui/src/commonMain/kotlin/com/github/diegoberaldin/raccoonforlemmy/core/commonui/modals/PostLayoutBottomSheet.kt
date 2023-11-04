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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class PostLayoutBottomSheet : Screen {

    @Composable
    override fun Content() {
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }

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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetHandle()
                Text(
                    modifier = Modifier.padding(start = Spacing.s, top = Spacing.s),
                    text = stringResource(MR.strings.settings_post_layout),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                val values = listOf(
                    PostLayout.Card,
                    PostLayout.Compact,
                    PostLayout.Full,
                )
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                ) {
                    for (value in values) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.m,
                            ).fillMaxWidth().onClick(
                                rememberCallback {
                                    notificationCenter.getObserver(NotificationCenterContractKeys.ChangePostLayout)
                                        ?.also {
                                            it.invoke(value)
                                        }
                                    navigationCoordinator.getBottomNavigator()?.hide()
                                },
                            ),
                        ) {
                            Text(
                                text = value.toReadableName(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        }
    }
}
