package com.github.diegoberaldin.raccoonforlemmy.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.github.diegoberaldin.raccoonforlemmy.MainView
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.home.ui.HomeTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        var loadingFinished = false
        installSplashScreen().setKeepOnScreenCondition {
            !loadingFinished
        }
        super.onCreate(savedInstanceState)

        // manage exit confirmation
        val navigationCoordinator = getNavigationCoordinator()
        val drawerCoordinator = getDrawerCoordinator()
        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // if the drawer is open, closes it
                if (drawerCoordinator.drawerOpened.value) {
                    lifecycleScope.launch {
                        drawerCoordinator.toggleDrawer()
                    }
                    return
                }

                // otherwise ask for confirmation
                if (navigationCoordinator.currentSection.value == TabNavigationSection.Home) {
                    // asks for confirmation
                    if (!navigationCoordinator.exitMessageVisible.value) {
                        navigationCoordinator.setExitMessageVisible(true)
                    }
                    return
                }

                // goes back to home
                with(navigationCoordinator) {
                    changeTab(HomeTab)
                    setCurrentSection(TabNavigationSection.Home)
                }
            }
        }
        navigationCoordinator.exitMessageVisible.onEach { exitMessageVisible ->
            backPressedCallback.isEnabled = !exitMessageVisible
        }.launchIn(lifecycleScope)
        onBackPressedDispatcher.addCallback(backPressedCallback)

        setContent {
            MainView(
                onLoadingFinished = {
                    loadingFinished = true
                },
            )
        }

        intent?.data?.toString()?.also {
            handleDeeplink(it)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.toString()?.also {
            runBlocking {
                delay(500)
                handleDeeplink(it)
            }
        }
    }

    private fun handleDeeplink(url: String) {
        val navigationCoordinator = getNavigationCoordinator()
        navigationCoordinator.submitDeeplink(url)
    }
}
