package com.github.diegoberaldin.raccoonforlemmy.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.github.diegoberaldin.raccoonforlemmy.MainView
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        var loadingFinished = false
        installSplashScreen().setKeepOnScreenCondition {
            !loadingFinished
        }
        super.onCreate(savedInstanceState)

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
