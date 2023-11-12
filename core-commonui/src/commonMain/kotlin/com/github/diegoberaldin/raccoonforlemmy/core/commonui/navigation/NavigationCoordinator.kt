package com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface NavigationCoordinator {

    val onDoubleTabSelection: Flow<Tab>
    val inboxUnread: StateFlow<Int>
    val deepLinkUrl: Flow<String?>
    val canPop: Boolean

    fun setCurrentSection(tab: Tab)
    fun submitDeeplink(url: String)
    fun setRootNavigator(value: Navigator?)
    fun setCanGoBackCallback(value: (() -> Boolean)?)
    fun getCanGoBackCallback(): (() -> Boolean)?
    fun setBottomBarScrollConnection(value: NestedScrollConnection?)
    fun getBottomBarScrollConnection(): NestedScrollConnection?
    fun setInboxUnread(count: Int)
    fun setBottomNavigator(value: BottomSheetNavigator?)
    fun showBottomSheet(screen: Screen)
    fun hideBottomSheet()
    fun pushScreen(screen: Screen)
    fun popScreen()
}
