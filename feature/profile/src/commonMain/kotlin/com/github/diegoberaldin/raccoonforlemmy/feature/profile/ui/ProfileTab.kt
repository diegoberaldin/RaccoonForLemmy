package com.github.diegoberaldin.raccoonforlemmy.feature.profile.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainScreen

object ProfileTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.AccountCircle)
            val title = LocalXmlStrings.current.navigationProfile
            return TabOptions(
                index = 2u,
                title = title,
                icon = icon,
            )
        }

    @Composable
    override fun Content() {
        TabNavigator(ProfileMainScreen)
    }
}
