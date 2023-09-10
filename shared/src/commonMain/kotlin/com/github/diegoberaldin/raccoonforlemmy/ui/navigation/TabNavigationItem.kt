package com.github.diegoberaldin.raccoonforlemmy.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator

@Composable
internal fun RowScope.TabNavigationItem(tab: Tab, withText: Boolean = true) {
    val tabNavigator = LocalTabNavigator.current
    val navigationCoordinator = remember { getNavigationCoordinator() }
    Box(
        modifier = Modifier.weight(1f)
            .fillMaxHeight()
            .onClick {
                tabNavigator.current = tab
                navigationCoordinator.setCurrentSection(tab)
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xxxs),
        ) {
            val color = if (tabNavigator.current == tab) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
            Icon(
                modifier = Modifier.size(36.dp),
                painter = tab.options.icon ?: rememberVectorPainter(Icons.Default.Home),
                contentDescription = null,
                tint = color,
            )
            if (withText) {
                Text(
                    modifier = Modifier,
                    text = tab.options.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
                Spacer(modifier = Modifier.height(Spacing.xxxs))
            }
        }
    }
}
