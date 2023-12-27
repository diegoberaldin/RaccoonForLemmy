package com.github.diegoberaldin.raccoonforlemmy.core.navigation

import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

private sealed interface NavigationEvent {

    data class Show(val screen: Screen) : NavigationEvent

    data object Hide : NavigationEvent
}

@OptIn(FlowPreview::class)
internal class DefaultNavigationCoordinator : NavigationCoordinator {

    override val currentSection = MutableStateFlow<TabNavigationSection?>(null)
    override val onDoubleTabSelection = MutableSharedFlow<TabNavigationSection>()
    override val deepLinkUrl = MutableSharedFlow<String>()
    override val inboxUnread = MutableStateFlow(0)
    override val canPop = MutableStateFlow(false)
    override val exitMessageVisible = MutableStateFlow(false)
    override val bottomSheetGesturesEnabled = MutableStateFlow(true)

    private var connection: NestedScrollConnection? = null
    private var navigator: Navigator? = null
    private var bottomNavigator: BottomSheetNavigator? = null
    private var tabNavigator: TabNavigator? = null
    private val scope = CoroutineScope(SupervisorJob())
    private var canGoBackCallback: (() -> Boolean)? = null
    private val bottomSheetChannel = Channel<NavigationEvent>()
    private val screenChannel = Channel<NavigationEvent>()

    companion object {
        private const val NAVIGATION_DELAY = 100L
        private const val BOTTOM_NAVIGATION_DELAY = 100L
        private const val DEEP_LINK_DELAY = 500L
    }

    init {
        scope.launch {
            bottomSheetChannel.receiveAsFlow().let { flow ->
                merge(
                    flow.take(1),
                    flow.drop(1).debounce(BOTTOM_NAVIGATION_DELAY)
                )
            }.onEach { evt ->
                when (evt) {
                    is NavigationEvent.Show -> {
                        bottomNavigator?.show(evt.screen)
                    }

                    NavigationEvent.Hide -> {
                        bottomNavigator?.hide()
                        setBottomSheetGesturesEnabled(true)
                    }
                }
            }.launchIn(this)
            screenChannel.receiveAsFlow()
                .let { flow ->
                    merge(
                        flow.take(1),
                        flow.drop(1).debounce(NAVIGATION_DELAY)
                    )
                }.onEach { evt ->
                    when (evt) {
                        is NavigationEvent.Show -> {
                            // make sure the new screen has a different key than the top of the stack
                            if (evt.screen.key != navigator?.lastItem?.key) {
                                navigator?.push(evt.screen)
                                canPop.value = navigator?.canPop == true
                            }
                        }

                        NavigationEvent.Hide -> {
                            navigator?.pop()
                            canPop.value = navigator?.canPop == true
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
        scope.launch {
            bottomSheetChannel.send(NavigationEvent.Hide)
        }
    }

    override fun popScreen() {
        scope.launch {
            screenChannel.send(NavigationEvent.Hide)
        }
    }

    override fun setExitMessageVisible(value: Boolean) {
        exitMessageVisible.value = value
    }

    override fun setBottomSheetGesturesEnabled(value: Boolean) {
        bottomSheetGesturesEnabled.value = value
    }

    override fun setTabNavigator(value: TabNavigator) {
        tabNavigator = value
    }

    override fun changeTab(value: Tab) {
        tabNavigator?.current = value
    }
}