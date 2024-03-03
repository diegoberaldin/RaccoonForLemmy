package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Padding
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.SettingsRow
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

@Composable
internal fun ProfileShortcutSection(
    modifier: Modifier = Modifier,
    isMod: Boolean = false,
    onOpenSaved: (() -> Unit)? = null,
    onOpenSubscriptions: (() -> Unit)? = null,
    onOpenDrafts: (() -> Unit)? = null,
    onOpenAllReports: (() -> Unit)? = null,
    onOpenModlog: (() -> Unit)? = null,
    onOpenComments: (() -> Unit)? = null,
    onOpenPosts: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(bottom = Spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        SettingsRow(
            modifier = modifier.padding(vertical = Spacing.xxs),
            icon = Icons.Default.Bookmark,
            title = LocalXmlStrings.current.navigationDrawerTitleBookmarks,
            disclosureIndicator = true,
            onTap = onOpenSaved,
        )

        SettingsRow(
            modifier = modifier.padding(vertical = Spacing.xxs),
            icon = Icons.Default.SettingsApplications,
            title = LocalXmlStrings.current.navigationDrawerTitleSubscriptions,
            disclosureIndicator = true,
            onTap = onOpenSubscriptions,
        )

        SettingsRow(
            modifier = modifier.padding(vertical = Spacing.xxs),
            icon = Icons.Default.Drafts,
            title = LocalXmlStrings.current.navigationDrawerTitleDrafts,
            disclosureIndicator = true,
            onTap = onOpenDrafts,
        )

        if (isMod) {
            SettingsRow(
                modifier = modifier.padding(vertical = Spacing.xxs),
                icon = Icons.Default.Report,
                title = LocalXmlStrings.current.reportListTitle,
                disclosureIndicator = true,
                onTap = onOpenAllReports,
            )

            SettingsRow(
                modifier = modifier.padding(vertical = Spacing.xxs),
                icon = Icons.AutoMirrored.Default.ListAlt,
                title = LocalXmlStrings.current.modlogTitle,
                disclosureIndicator = true,
                onTap = onOpenModlog,
            )
            SettingsRow(
                modifier = modifier.padding(vertical = Spacing.xxs),
                icon = Icons.AutoMirrored.Default.Message,
                // TODO: l10n
                title = "Moderated comments",
                disclosureIndicator = true,
                onTap = onOpenComments,
            )
            SettingsRow(
                modifier = modifier.padding(vertical = Spacing.xxs),
                icon = Icons.Default.Padding,
                // TODO: l10n
                title = "Moderated posts",
                disclosureIndicator = true,
                onTap = onOpenPosts,
            )
        }
    }
}