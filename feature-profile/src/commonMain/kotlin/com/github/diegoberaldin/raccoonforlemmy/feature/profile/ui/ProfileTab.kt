package com.github.diegoberaldin.raccoonforlemmy.feature.profile.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.main.ProfileMainScreen
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.di.staticString
import dev.icerock.moko.resources.desc.desc

object ProfileTab : Tab {

    override val options: TabOptions
        @Composable get() {
            val icon = rememberVectorPainter(Icons.Default.AccountCircle)
            val languageRepository = remember { getLanguageRepository() }
            val lang by languageRepository.currentLanguage.collectAsState()
            return remember(lang) {
                val title = staticString(MR.strings.navigation_profile.desc())
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        TabNavigator(ProfileMainScreen)
    }
}
