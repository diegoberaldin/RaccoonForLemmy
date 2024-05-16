package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxScreen

object InboxTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Inbox)
            val title = LocalXmlStrings.current.navigationInbox
            return TabOptions(
                index = 3u,
                title = title,
                icon = icon,
            )
        }

    @Composable
    override fun Content() {
        TabNavigator(InboxScreen)
    }
}
