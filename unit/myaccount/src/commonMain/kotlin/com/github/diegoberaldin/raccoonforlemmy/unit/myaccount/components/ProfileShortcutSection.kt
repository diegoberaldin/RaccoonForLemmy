package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

@Composable
internal fun ProfileShortcutSection(
    modifier: Modifier = Modifier,
    isMod: Boolean = false,
    onOpenSubscriptions: (() -> Unit)? = null,
    onOpenSaved: (() -> Unit)? = null,
    onOpenDrafts: (() -> Unit)? = null,
    onOpenVotes: (() -> Unit)? = null,
    onOpenModeratorZone: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(bottom = Spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        SettingsRow(
            icon = Icons.Default.SettingsApplications,
            title = LocalXmlStrings.current.navigationDrawerTitleSubscriptions,
            disclosureIndicator = true,
            onTap = onOpenSubscriptions,
        )

        SettingsRow(
            icon = Icons.Default.Bookmark,
            title = LocalXmlStrings.current.navigationDrawerTitleBookmarks,
            disclosureIndicator = true,
            onTap = onOpenSaved,
        )

        SettingsRow(
            icon = Icons.Default.Drafts,
            title = LocalXmlStrings.current.navigationDrawerTitleDrafts,
            disclosureIndicator = true,
            onTap = onOpenDrafts,
        )

        SettingsRow(
            icon = Icons.Default.ThumbsUpDown,
            title = LocalXmlStrings.current.profileUpvotesDownvotes,
            disclosureIndicator = true,
            onTap = onOpenVotes,
        )

        if (isMod) {
            SettingsRow(
                icon = Icons.AutoMirrored.Default.Message,
                title = LocalXmlStrings.current.moderatorZoneTitle,
                disclosureIndicator = true,
                onTap = onOpenModeratorZone,
            )
        }
    }
}