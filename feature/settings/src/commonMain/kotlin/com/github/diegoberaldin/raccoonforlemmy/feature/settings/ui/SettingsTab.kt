package com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.main.SettingsScreen

object SettingsTab : Tab {

    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Settings)
            val title = LocalXmlStrings.current.navigationSettings
            return TabOptions(
                index = 4u,
                title = title,
                icon = icon,
            )
        }

    @Composable
    override fun Content() {
        Navigator(SettingsScreen())
    }
}
