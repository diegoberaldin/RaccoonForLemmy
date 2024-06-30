package com.github.diegoberaldin.raccoonforlemmy.core.navigation

import androidx.compose.runtime.Stable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

sealed interface TabNavigationSection {
    data object Home : TabNavigationSection

    data object Explore : TabNavigationSection

    data object Profile : TabNavigationSection

    data object Inbox : TabNavigationSection

    data object Settings : TabNavigationSection
}

sealed interface ComposeEvent {
    data class WithUrl(
        val url: String,
    ) : ComposeEvent

    data class WithText(
        val text: String,
    ) : ComposeEvent
}

sealed interface SideMenuEvents {
    data class Open(
        val screen: Screen,
    ) : SideMenuEvents

    data object Close : SideMenuEvents
}

@Stable
interface NavigationCoordinator {
    val currentSection: StateFlow<TabNavigationSection?>
    val onDoubleTabSelection: Flow<TabNavigationSection>
    val inboxUnread: StateFlow<Int>
    val deepLinkUrl: Flow<String?>
    val composeEvents: Flow<ComposeEvent?>
    val canPop: StateFlow<Boolean>
    val exitMessageVisible: StateFlow<Boolean>
    val sideMenuEvents: Flow<SideMenuEvents>
    val sideMenuOpened: StateFlow<Boolean>
    val globalMessage: Flow<String>

    fun setCurrentSection(section: TabNavigationSection)

    fun submitDeeplink(url: String)

    fun submitComposeEvent(event: ComposeEvent)

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

    fun setExitMessageVisible(value: Boolean)

    fun setTabNavigator(value: TabNavigator)

    fun changeTab(value: Tab)

    fun openSideMenu(screen: Screen)

    fun closeSideMenu()

    fun showGlobalMessage(
        message: String,
        delay: Duration = Duration.ZERO,
    )

    fun popUntilRoot()
}
