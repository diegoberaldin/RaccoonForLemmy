package com.github.diegoberaldin.raccoonforlemmy.core.commonui.web

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomWebView
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.rememberWebViewNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.getShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback

class WebViewScreen(
    private val url: String,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        var shareHelper = remember { getShareHelper() }
        val drawerCoordinator = remember { getDrawerCoordinator() }

        DisposableEffect(key) {
            drawerCoordinator.setGesturesEnabled(false)
            onDispose {
                drawerCoordinator.setGesturesEnabled(true)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                rememberCallback {
                                    navigator?.pop()
                                },
                            ),
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    actions = {
                        Icon(
                            modifier = Modifier.onClick(
                                rememberCallback {
                                    shareHelper.share(url, "text/plain")
                                },
                            ),
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier.padding(paddingValues)
            ) {
                val navigationCoordinator = remember { getNavigationCoordinator() }
                val webNavigator = rememberWebViewNavigator()

                DisposableEffect(key) {
                    navigationCoordinator.setCanGoBackCallback {
                        val result = webNavigator.canGoBack
                        if (result) {
                            webNavigator.goBack()
                            return@setCanGoBackCallback false
                        }
                        true
                    }
                    onDispose {
                        navigationCoordinator.setCanGoBackCallback(null)
                    }
                }

                CustomWebView(
                    modifier = Modifier.fillMaxSize(),
                    navigator = webNavigator,
                    scrollConnection = scrollBehavior.nestedScrollConnection,
                    url = url,
                )
            }
        }
    }
}