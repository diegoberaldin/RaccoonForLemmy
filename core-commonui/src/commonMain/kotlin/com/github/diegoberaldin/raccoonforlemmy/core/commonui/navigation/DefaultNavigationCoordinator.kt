package com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


private const val NAVIGATION_DELAY = 100L
private const val BOTTOM_NAVIGATION_DELAY = 100L
private const val DEEP_LINK_DELAY = 500L

internal class DefaultNavigationCoordinator : NavigationCoordinator {

    override val onDoubleTabSelection = MutableSharedFlow<Tab>()
    override val deepLinkUrl = MutableSharedFlow<String>()
    override val inboxUnread = MutableStateFlow(0)
    override val canPop: Boolean
        get() = navigator?.canPop == true

    private var connection: NestedScrollConnection? = null
    private var navigator: Navigator? = null
    private var bottomNavigator: BottomSheetNavigator? = null
    private var currentTab: Tab? = null
    private val scope = CoroutineScope(SupervisorJob())
    private var canGoBackCallback: (() -> Boolean)? = null
    private var job: Job? = null

    override fun setRootNavigator(value: Navigator?) {
        navigator = value
    }

    override fun setBottomBarScrollConnection(value: NestedScrollConnection?) {
        connection = value
    }

    override fun getBottomBarScrollConnection() = connection

    override fun setCurrentSection(tab: Tab) {
        val oldTab = currentTab
        currentTab = tab
        if (tab == oldTab) {
            scope.launch {
                onDoubleTabSelection.emit(tab)
            }
        }
    }

    override fun submitDeeplink(url: String) {
        scope.launch {
            delay(DEEP_LINK_DELAY)
            runCatching {
                ensureActive()
                deepLinkUrl.emit(url)
            }
        }
    }

    override fun setCanGoBackCallback(value: (() -> Boolean)?) {
        canGoBackCallback = value
    }

    override fun getCanGoBackCallback(): (() -> Boolean)? = canGoBackCallback

    override fun setInboxUnread(count: Int) {
        inboxUnread.value = count
    }

    override fun setBottomNavigator(value: BottomSheetNavigator?) {
        bottomNavigator = value
    }

    override fun showBottomSheet(screen: Screen) {
        job?.cancel()
        job = scope.launch {
            delay(BOTTOM_NAVIGATION_DELAY)
            runCatching {
                ensureActive()
                bottomNavigator?.show(screen)
            }
        }
    }

    override fun pushScreen(screen: Screen) {
        job?.cancel()
        job = scope.launch {
            delay(NAVIGATION_DELAY)
            runCatching {
                ensureActive()
                navigator?.push(screen)
            }
        }
    }

    override fun hideBottomSheet() {
        job?.cancel()
        job = scope.launch {
            delay(BOTTOM_NAVIGATION_DELAY)
            runCatching {
                ensureActive()
                bottomNavigator?.hide()
            }
        }
    }

    override fun popScreen() {
        job?.cancel()
        job = scope.launch {
            delay(NAVIGATION_DELAY)
            runCatching {
                ensureActive()
                navigator?.pop()
            }
        }
    }
}