package com.github.diegoberaldin.raccoonforlemmy.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback

internal class ProfileSideMenu(
    private val isModerator: Boolean = false
) : Screen {

    @Composable
    override fun Content() {
        val notificationCenter = remember { getNotificationCenter() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                .padding(top = Spacing.xl)
        ) {
            SettingsRow(
                title = LocalXmlStrings.current.manageAccountsTitle,
                icon = Icons.Default.ManageAccounts,
                onTap = rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.ManageAccounts)
                },
            )
            SettingsRow(
                title = LocalXmlStrings.current.navigationDrawerTitleSubscriptions,
                icon = Icons.Default.Subscriptions,
                onTap = rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.ManageSubscriptions)
                },
            )
            SettingsRow(
                title = LocalXmlStrings.current.navigationDrawerTitleBookmarks,
                icon = Icons.Default.Bookmark,
                onTap = rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.Bookmarks)
                },
            )
            SettingsRow(
                title = LocalXmlStrings.current.navigationDrawerTitleDrafts,
                icon = Icons.Default.Drafts,
                onTap = rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.Drafts)
                },
            )
            SettingsRow(
                title = LocalXmlStrings.current.profileUpvotesDownvotes,
                icon = Icons.Default.ThumbsUpDown,
                onTap = rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.Votes)
                },
            )

            if (isModerator) {
                SettingsRow(
                    title = LocalXmlStrings.current.moderatorZoneTitle,
                    icon = Icons.Default.Shield,
                    onTap = rememberCallback {
                        notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.ModeratorZone)
                    },
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.s)
            )

            SettingsRow(
                title = LocalXmlStrings.current.actionLogout,
                icon = Icons.AutoMirrored.Default.Logout,
                onTap = rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.Logout)
                },
            )
        }
    }
}
