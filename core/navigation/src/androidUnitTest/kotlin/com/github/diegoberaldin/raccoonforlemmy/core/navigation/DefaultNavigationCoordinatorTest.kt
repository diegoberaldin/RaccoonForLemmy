package com.github.diegoberaldin.raccoonforlemmy.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.turbine.test
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultNavigationCoordinatorTest {

    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    private val sut = DefaultNavigationCoordinator()

    @Test
    fun whenSetCurrentSection_thenValueIsUpdated() = runTest {
        val initial = sut.currentSection.value
        assertNull(initial)

        sut.setCurrentSection(TabNavigationSection.Settings)

        val value = sut.currentSection.value
        assertEquals(TabNavigationSection.Settings, value)
    }

    @Test
    fun whenSetCurrentSectionTwice_thenOnDoubleTabSelectionTriggered() = runTest {
        sut.setCurrentSection(TabNavigationSection.Profile)
        launch {
            delay(250)
            sut.setCurrentSection(TabNavigationSection.Profile)
        }
        sut.onDoubleTabSelection.test {
            val section = awaitItem()
            assertEquals(TabNavigationSection.Profile, section)
        }
    }

    @Test
    fun givenNavigatorCanPop_whenRootNavigatorSet_thenCanPopIsUpdated() = runTest {
        val initial = sut.canPop.value
        assertFalse(initial)
        val navigator = mockk<Navigator> {
            every { canPop } returns true
        }

        sut.setRootNavigator(navigator)

        val value = sut.canPop.value
        assertTrue(value)
    }

    @Test
    fun whenSubmitDeeplink_thenValueIsEmitted() = runTest {
        val url = "deeplink-url"
        sut.submitDeeplink(url)

        sut.deepLinkUrl.test {
            val value = awaitItem()
            assertEquals(url, value)
        }
    }

    @Test
    fun whenSetInboxUnread_thenValueIsUpdated() = runTest {
        val count = 5
        sut.setInboxUnread(count)
        val value = sut.inboxUnread.value
        assertEquals(count, value)
    }

    @Test
    fun whenSetExitMessageVisible_thenValueIsUpdated() = runTest {
        val initial = sut.exitMessageVisible.value
        assertFalse(initial)

        sut.setExitMessageVisible(true)
        val value = sut.exitMessageVisible.value
        assertTrue(value)
    }

    @Test
    fun whenChangeTab_thenCurrentTabIsUpdated() = runTest {
        val tabSlot = slot<Tab>()
        val navigator = mockk<TabNavigator>(relaxUnitFun = true) {
            every { current = capture(tabSlot) } answers {}
        }
        val tab = object : Tab {
            override val options @Composable get() = TabOptions(index = 0u, "title")

            @Composable
            override fun Content() {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        sut.setTabNavigator(navigator)

        sut.changeTab(tab)

        val value = tabSlot.captured
        assertEquals(tab, value)
    }

    @Test
    fun whenPushScreen_thenInteractionsAreAsExpected() = runTest {
        val previous = object : Screen {

            override val key: ScreenKey = "old"

            @Composable
            override fun Content() {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        val screen = object : Screen {

            override val key: ScreenKey = "new"

            @Composable
            override fun Content() {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        val navigator = mockk<Navigator>(relaxUnitFun = true) {
            every { canPop } returns true
            every { lastItem } returns previous
        }
        sut.setRootNavigator(navigator)

        sut.pushScreen(screen)

        val canPop = sut.canPop.value
        assertTrue(canPop)
        verify {
            navigator.push(screen)
        }
    }

    @Test
    fun whenPushScreenTwice_thenInteractionsAreAsExpected() = runTest {
        val screen = object : Screen {

            override val key: ScreenKey = ""

            @Composable
            override fun Content() {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        val navigator = mockk<Navigator>(relaxUnitFun = true) {
            every { canPop } returns true
            every { lastItem } returns screen
        }
        sut.setRootNavigator(navigator)

        sut.pushScreen(screen)

        val canPop = sut.canPop.value
        assertTrue(canPop)
        verify(inverse = true) {
            navigator.push(screen)
        }
    }

    @Test
    fun whenPopScreen_thenInteractionsAreAsExpected() = runTest {
        val navigator = mockk<Navigator>(relaxUnitFun = true) {
            every { pop() } returns true
            every { canPop } returns false
        }
        sut.setRootNavigator(navigator)

        sut.popScreen()

        val canPop = sut.canPop.value
        assertFalse(canPop)
        verify {
            navigator.pop()
        }
    }

    @Test
    fun whenShowBottomSheet_thenInteractionsAreAsExpected() = runTest {
        val screen = object : Screen {
            @Composable
            override fun Content() {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        val navigator = mockk<BottomSheetNavigator>(relaxUnitFun = true)
        sut.setBottomNavigator(navigator)

        sut.showBottomSheet(screen)

        verify {
            navigator.show(screen)
        }
    }

    @Test
    fun whenHideBottomSheet_thenInteractionsAreAsExpected() = runTest {
        val navigator = mockk<BottomSheetNavigator>(relaxUnitFun = true) {
            every { isVisible } returns true
        }
        sut.setBottomNavigator(navigator)

        sut.hideBottomSheet()

        verify {
            navigator.hide()
        }
    }

    @Test
    fun whenShowSideMenu_thenInteractionsAreAsExpected() = runTest {
        val screen = object : Screen {
            @Composable
            override fun Content() {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        launch {
            sut.openSideMenu(screen)
        }

        sut.sideMenuEvents.test {
            val item = awaitItem()
            assertEquals(SideMenuEvents.Open(screen), item)
        }
    }

    @Test
    fun whenCloseSideMenu_thenInteractionsAreAsExpected() = runTest {
        launch {
            sut.closeSideMenu()
        }

        sut.sideMenuEvents.test {
            val item = awaitItem()
            assertEquals(SideMenuEvents.Close, item)
        }
    }

    @Test
    fun whenShowGlobalMEssagethenInteractionsAreAsExpected() = runTest {
        val message = "test message"
        launch {
            sut.showGlobalMessage(message)
        }

        sut.globalMessage.test {
            val item = awaitItem()
            assertEquals(message, item)
        }
    }
}