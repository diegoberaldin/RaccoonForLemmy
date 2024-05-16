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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHeader
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.getPrettyDuration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class DurationBottomSheetType {
    ZOMBIE_MODE_INTERVAL,
    INBOX_CHECK_PERIOD,
}

class DurationBottomSheet(
    private val values: List<Duration> =
        listOf(
            1.seconds,
            2.seconds,
            3.seconds,
            5.seconds,
            10.seconds,
        ),
    private val type: DurationBottomSheetType = DurationBottomSheetType.ZOMBIE_MODE_INTERVAL,
) : Screen {
    @Composable
    override fun Content() {
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
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
            val title =
                when (type) {
                    DurationBottomSheetType.ZOMBIE_MODE_INTERVAL -> LocalXmlStrings.current.settingsZombieModeInterval
                    DurationBottomSheetType.INBOX_CHECK_PERIOD -> LocalXmlStrings.current.settingsInboxBackgroundCheckPeriod
                }
            BottomSheetHeader(title)
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
                                )
                                .fillMaxWidth()
                                .onClick(
                                    onClick = {
                                        val event =
                                            when (type) {
                                                DurationBottomSheetType.ZOMBIE_MODE_INTERVAL ->
                                                    NotificationCenterEvent.ChangeZombieInterval(value)

                                                DurationBottomSheetType.INBOX_CHECK_PERIOD ->
                                                    NotificationCenterEvent.ChangeInboxBackgroundCheckPeriod(value)
                                            }
                                        notificationCenter.send(event)
                                        navigationCoordinator.hideBottomSheet()
                                    },
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                value.getPrettyDuration(
                                    secondsLabel = LocalXmlStrings.current.postSecondShort,
                                    minutesLabel = LocalXmlStrings.current.postMinuteShort,
                                    hoursLabel = LocalXmlStrings.current.postHourShort,
                                ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
