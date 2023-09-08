package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.ProfileLoggedScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.notlogged.ProfileNotLoggedContent
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.getProfileScreenModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.login.LoginBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.di.staticString
import dev.icerock.moko.resources.desc.desc

internal class ProfileContentScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getProfileScreenModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                val languageRepository = remember { getLanguageRepository() }
                val lang by languageRepository.currentLanguage.collectAsState()
                val title by remember(lang) {
                    mutableStateOf(staticString(MR.strings.navigation_profile.desc()))
                }
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        if (uiState.currentUser != null) {
                            Image(
                                modifier = Modifier.onClick {
                                    model.reduce(ProfileContentMviModel.Intent.Logout)
                                },
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            )
                        }
                    },
                )
            },
        ) {
            val bottomSheetNavigator = LocalBottomSheetNavigator.current
            Box(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(it),
                contentAlignment = Alignment.Center,
            ) {
                if (!uiState.initial) {
                    val user = uiState.currentUser
                    if (user == null) {
                        ProfileNotLoggedContent(
                            onLogin = {
                                bottomSheetNavigator.show(
                                    LoginBottomSheet(
                                        onHide = {
                                            bottomSheetNavigator.hide()
                                        },
                                    ),
                                )
                            },
                        ).Content()
                    } else {
                        ProfileLoggedScreen(
                            user = user,
                        ).Content()
                    }
                }
            }
        }
    }
}
