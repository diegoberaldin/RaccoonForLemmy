package com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHeader
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.Locales
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLanguageFlag
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLanguageName

class LanguageBottomSheet : Screen {
    @Composable
    override fun Content() {
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        Column(
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.safeContent)
                    .padding(
                        top = Spacing.s,
                        start = Spacing.s,
                        end = Spacing.s,
                        bottom = Spacing.m,
                    ),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            BottomSheetHeader(LocalStrings.current.settingsLanguage)
            val values =
                listOf(
                    Locales.AR,
                    Locales.BG,
                    Locales.CS,
                    Locales.DA,
                    Locales.DE,
                    Locales.ET,
                    Locales.EL,
                    Locales.EN,
                    Locales.ES,
                    Locales.EO,
                    Locales.FR,
                    Locales.GA,
                    Locales.HR,
                    Locales.IT,
                    Locales.LV,
                    Locales.LT,
                    Locales.HU,
                    Locales.MT,
                    Locales.NL,
                    Locales.NO,
                    Locales.PL,
                    Locales.PT,
                    Locales.PT_BR,
                    Locales.RO,
                    Locales.RU,
                    Locales.SK,
                    Locales.SL,
                    Locales.SQ,
                    Locales.SR,
                    Locales.FI,
                    Locales.SE,
                    Locales.TOK,
                    Locales.TR,
                    Locales.UK,
                )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                for (value in values) {
                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.s,
                            )
                                .fillMaxWidth()
                                .onClick(
                                    onClick = {
                                        notificationCenter.send(
                                            NotificationCenterEvent.ChangeLanguage(value),
                                        )
                                        navigationCoordinator.hideBottomSheet()
                                    },
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                buildAnnotatedString {
                                    with(value) {
                                        append(toLanguageFlag())
                                        append("  ")
                                        append(toLanguageName())
                                    }
                                },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
