package com.github.diegoberaldin.raccoonforlemmy.feature.search.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.unit.explore.ExploreScreen

object ExploreTab : Tab {
    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.Explore)
            val title = LocalXmlStrings.current.navigationSearch
            return TabOptions(
                index = 1u,
                title = title,
                icon = icon,
            )
        }

    @Composable
    override fun Content() {
        Navigator(ExploreScreen())
    }
}
