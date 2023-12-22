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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHandle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.BlockActionType
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class BlockBottomSheet(
    private val userName: String? = null,
    private val userId: Int? = null,
    private val communityName: String? = null,
    private val communityId: Int? = null,
    private val instanceName: String? = null,
    private val instanceId: Int? = null,
    private val userInstanceName: String? = null,
    private val userInstanceId: Int? = null,
) : Screen {

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
                    text = stringResource(MR.strings.community_detail_block),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
                ) {
                    val values: List<Triple<BlockActionType, Int, String>> = buildList {
                        if (userName != null && userId != null) {
                            this += Triple(
                                BlockActionType.User,
                                userId,
                                userName,
                            )
                        }
                        if (communityName != null && communityId != null) {
                            this += Triple(
                                BlockActionType.Community,
                                communityId,
                                communityName,
                            )
                        }
                        if (instanceName != null && instanceId != null) {
                            this += Triple(
                                BlockActionType.Instance,
                                instanceId,
                                instanceName,
                            )
                        }
                        if (userInstanceName != null && userInstanceId != null && userInstanceName != instanceName) {
                            this += Triple(
                                BlockActionType.Instance,
                                userInstanceId,
                                userInstanceName,
                            )
                        }
                    }
                    for (value in values) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.m,
                            ).fillMaxWidth().onClick(
                                onClick = rememberCallback {
                                    val event = when (value.first) {
                                        BlockActionType.Community -> NotificationCenterEvent.BlockActionSelected(
                                            communityId = value.second
                                        )

                                        BlockActionType.Instance ->
                                            NotificationCenterEvent.BlockActionSelected(
                                                instanceId = value.second
                                            )

                                        BlockActionType.User -> NotificationCenterEvent.BlockActionSelected(
                                            userId = value.second
                                        )
                                    }
                                    notificationCenter.send(event)
                                    navigationCoordinator.hideBottomSheet()
                                },
                            ),
                        ) {
                            val valueText = buildString {
                                append(value.first.toReadableName())
                                val additionalText = value.third
                                if (additionalText.isNotEmpty()) {
                                    append("\n")
                                    append("(")
                                    append(additionalText)
                                    append(")")
                                }
                            }
                            Text(
                                text = valueText,
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
