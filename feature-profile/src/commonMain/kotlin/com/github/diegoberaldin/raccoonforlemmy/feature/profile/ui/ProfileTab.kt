package com.github.diegoberaldin.raccoonforlemmy.feature.profile.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.di.getApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.ProfileContentScreen

object ProfileTab : Tab {

    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.AccountCircle)
            val apiConfigurationRepository = remember { getApiConfigurationRepository() }
            val instance by apiConfigurationRepository.instance.collectAsState("")

            return remember(instance) {
                TabOptions(
                    index = 2u,
                    title = instance,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        ProfileContentScreen().Content()
    }
}
