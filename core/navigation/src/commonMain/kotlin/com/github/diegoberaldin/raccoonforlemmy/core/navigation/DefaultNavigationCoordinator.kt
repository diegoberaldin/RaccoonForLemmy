package com.github.diegoberaldin.raccoonforlemmy.core.navigation

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private sealed interface NavigationEvent {

    data class Show(val screen: Screen) : NavigationEvent
}

internal class DefaultNavigationCoordinator : NavigationCoordinator {

    override val currentSection = MutableStateFlow<TabNavigationSection?>(null)
    override val onDoubleTabSelection = MutableSharedFlow<TabNavigationSection>()
    override val deepLinkUrl = MutableSharedFlow<String>()
    override val inboxUnread = MutableStateFlow(0)
    override val canPop = MutableStateFlow(false)
    override val exitMessageVisible = MutableStateFlow(false)

    private var connection: NestedScrollConnection? = null
    private var navigator: Navigator? = null
    private var bottomNavigator: BottomSheetNavigator? = null
    private var tabNavigator: TabNavigator? = null
    private val scope = CoroutineScope(SupervisorJob())
    private var canGoBackCallback: (() -> Boolean)? = null
    private val bottomSheetChannel = Channel<NavigationEvent>()
    private val screenChannel = Channel<NavigationEvent>()

    companion object {
        private const val DEEP_LINK_DELAY = 500L
    }

    init {
        scope.launch {
            bottomSheetChannel.receiveAsFlow().onEach { evt ->
                when (evt) {
                    is NavigationEvent.Show -> {
                        bottomNavigator?.show(evt.screen)
                    }
                }
            }.launchIn(this)
            screenChannel.receiveAsFlow().onEach { evt ->
                when (evt) {
                    is NavigationEvent.Show -> {
                        // make sure the new screen has a different key than the top of the stack
                        if (evt.screen.key != navigator?.lastItem?.key) {
                            navigator?.push(evt.screen)
                            canPop.value = navigator?.canPop == true
                        }
                    }
                }
            }.launchIn(this)
        }
    }

    override fun setRootNavigator(value: Navigator?) {
        navigator = value
        canPop.value = value?.canPop == true
    }

    override fun setBottomBarScrollConnection(value: NestedScrollConnection?) {
        connection = value
    }

    override fun getBottomBarScrollConnection() = connection

    override fun setCurrentSection(section: TabNavigationSection) {
        currentSection.getAndUpdate { oldValue ->
            if (section == oldValue) {
                scope.launch {
                    onDoubleTabSelection.emit(section)
                }
            }
            section
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
        scope.launch {
            bottomSheetChannel.send(NavigationEvent.Show(screen))
        }
    }

    override fun pushScreen(screen: Screen) {
        scope.launch {
            screenChannel.send(NavigationEvent.Show(screen))
        }
    }

    override fun hideBottomSheet() {
        bottomNavigator?.hide()
    }

    override fun popScreen() {
        navigator?.pop()
        canPop.value = navigator?.canPop == true
    }

    override fun setExitMessageVisible(value: Boolean) {
        exitMessageVisible.value = value
    }

    override fun setTabNavigator(value: TabNavigator) {
        tabNavigator = value
    }

    override fun changeTab(value: Tab) {
        tabNavigator?.current = value
    }
}
