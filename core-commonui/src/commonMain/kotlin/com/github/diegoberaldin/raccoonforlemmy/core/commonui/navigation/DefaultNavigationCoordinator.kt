package com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal class DefaultNavigationCoordinator : NavigationCoordinator {

    override val onDoubleTabSelection = MutableSharedFlow<Tab>()

    private var connection: NestedScrollConnection? = null
    private var navigator: Navigator? = null
    private var currentTab: Tab? = null
    private val scope = CoroutineScope(SupervisorJob())

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
}