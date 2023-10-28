package com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class DefaultNavigationCoordinator : NavigationCoordinator {

    override val onDoubleTabSelection = MutableSharedFlow<Tab>()
    override val deepLinkUrl = MutableSharedFlow<String>()

    private var connection: NestedScrollConnection? = null
    private var navigator: Navigator? = null
    private var currentTab: Tab? = null
    private val scope = CoroutineScope(SupervisorJob())
    private var canGoBackCallback: (() -> Boolean)? = null
    override val inboxUnread = MutableStateFlow(0)

    override fun setRootNavigator(value: Navigator?) {
        navigator = value
    }

    override fun getRootNavigator(): Navigator? = navigator

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
            delay(500)
            deepLinkUrl.emit(url)
        }
    }

    override fun setCanGoBackCallback(value: (() -> Boolean)?) {
        canGoBackCallback = value
    }

    override fun getCanGoBackCallback(): (() -> Boolean)? = canGoBackCallback

    override fun setInboxUnread(count: Int) {
        inboxUnread.value = count
    }
}