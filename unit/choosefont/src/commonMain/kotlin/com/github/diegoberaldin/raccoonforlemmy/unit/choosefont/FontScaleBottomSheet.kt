package com.github.diegoberaldin.raccoonforlemmy.unit.choosefont

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
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.FontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.scaleFactor
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toFontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ContentFontClass
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.BottomSheetHeader
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick

private val defaultChoices: List<Float> = listOf(
    FontScale.Largest,
    FontScale.Larger,
    FontScale.Large,
    FontScale.Normal,
    FontScale.Small,
    FontScale.Smaller,
    FontScale.Smallest,
).map { it.scaleFactor }


class FontScaleBottomSheet(
    private val values: List<Float> = defaultChoices,
    private val contentClass: ContentFontClass? = null,
) : Screen {

    @Composable
    override fun Content() {
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        Column(
            modifier = Modifier
                .padding(
                    top = Spacing.s,
                    start = Spacing.s,
                    end = Spacing.s,
                    bottom = Spacing.m,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            val title = when (contentClass) {
                ContentFontClass.Title -> LocalXmlStrings.current.settingsTitleFontScale
                ContentFontClass.Body -> LocalXmlStrings.current.settingsContentFontScale
                ContentFontClass.Comment -> LocalXmlStrings.current.settingsCommentFontScale
                ContentFontClass.AncillaryText -> LocalXmlStrings.current.settingsAncillaryFontScale
                else -> LocalXmlStrings.current.settingsUiFontScale
            }
            BottomSheetHeader(title)
            Text(
                modifier = Modifier.padding(
                    start = Spacing.s,
                    top = Spacing.s,
                    end = Spacing.s,
                ),
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                for (value in values) {
                    val fontScale = value.toFontScale()
                    Row(
                        modifier = Modifier
                            .padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.s,
                            )
                            .fillMaxWidth()
                            .onClick(
                                onClick = {
                                    notificationCenter.send(
                                        if (contentClass != null) {
                                            NotificationCenterEvent.ChangeContentFontSize(
                                                value = value,
                                                contentClass = contentClass
                                            )
                                        } else {
                                            NotificationCenterEvent.ChangeUiFontSize(value)
                                        }
                                    )
                                    navigationCoordinator.hideBottomSheet()
                                },
                            ),
                    ) {
                        val originalFontSize = MaterialTheme.typography.bodyLarge.fontSize
                        Text(
                            text = fontScale.toReadableName(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = originalFontSize * value,
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}
