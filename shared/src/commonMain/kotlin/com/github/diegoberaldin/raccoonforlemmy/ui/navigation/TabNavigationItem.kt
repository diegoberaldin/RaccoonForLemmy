package com.github.diegoberaldin.raccoonforlemmy.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.IconSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomImage
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.ui.InboxTab
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.ui.ProfileTab
import com.github.diegoberaldin.raccoonforlemmy.feature.search.ui.ExploreTab
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.SettingsTab

@Composable
internal fun RowScope.TabNavigationItem(
    tab: Tab,
    withText: Boolean = true,
    customIconUrl: String? = null,
) {
    val tabNavigator = LocalTabNavigator.current
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val unread by navigationCoordinator.inboxUnread.collectAsState()
    val color =
        if (tabNavigator.current == tab) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }

    NavigationBarItem(
        onClick = {
            tabNavigator.current = tab
            val section =
                when (tab) {
                    ExploreTab -> TabNavigationSection.Explore
                    ProfileTab -> TabNavigationSection.Profile
                    InboxTab -> TabNavigationSection.Inbox
                    SettingsTab -> TabNavigationSection.Settings
                    else -> TabNavigationSection.Home
                }
            navigationCoordinator.setCurrentSection(section)
        },
        selected = tabNavigator.current == tab,
        icon = {
            val content = @Composable {
                if (customIconUrl != null) {
                    val iconSize = IconSize.m
                    CustomImage(
                        url = customIconUrl,
                        modifier =
                            Modifier
                                .size(iconSize)
                                .clip(RoundedCornerShape(iconSize / 2)),
                    )
                } else {
                    Icon(
                        painter = tab.options.icon ?: rememberVectorPainter(Icons.Default.Home),
                        contentDescription = null,
                        tint = color,
                    )
                }
            }
            val inboxTitle = LocalStrings.current.navigationInbox
            if (tab.options.title == inboxTitle && unread > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            modifier = Modifier.padding(top = Spacing.s),
                        ) {
                            Text(
                                text =
                                    if (unread <= 99) {
                                        unread.toString()
                                    } else {
                                        "99+"
                                    },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    },
                ) {
                    content()
                }
            } else {
                content()
            }
        },
        label = {
            if (withText) {
                Text(
                    modifier = Modifier,
                    text = tab.options.title,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}
