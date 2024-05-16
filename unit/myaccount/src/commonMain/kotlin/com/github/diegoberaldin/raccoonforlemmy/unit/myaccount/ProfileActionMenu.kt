package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback

@Composable
internal fun ProfileActionMenu(
    modifier: Modifier = Modifier,
    isModerator: Boolean = false,
) {
    val notificationCenter = remember { getNotificationCenter() }
    Column(
        modifier = modifier,
    ) {
        SettingsRow(
            title = LocalXmlStrings.current.navigationDrawerTitleSubscriptions,
            icon = Icons.Default.Book,
            onTap =
                rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.ManageSubscriptions)
                },
        )
        SettingsRow(
            title = LocalXmlStrings.current.navigationDrawerTitleBookmarks,
            icon = Icons.Default.Bookmark,
            onTap =
                rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.Bookmarks)
                },
        )
        SettingsRow(
            title = LocalXmlStrings.current.navigationDrawerTitleDrafts,
            icon = Icons.Default.Drafts,
            onTap =
                rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.Drafts)
                },
        )
        SettingsRow(
            title = LocalXmlStrings.current.profileUpvotesDownvotes,
            icon = Icons.Default.ThumbsUpDown,
            onTap =
                rememberCallback {
                    notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.Votes)
                },
        )

        if (isModerator) {
            SettingsRow(
                title = LocalXmlStrings.current.moderatorZoneTitle,
                icon = Icons.Default.Shield,
                onTap =
                    rememberCallback {
                        notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.ModeratorZone)
                    },
            )
        }
    }
}
