package com.github.diegoberaldin.raccoonforlemmy.core.navigation

import app.cash.turbine.test
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultDrawerCoordinatorTest {

    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    private val sut = DefaultDrawerCoordinator()

    @Test
    fun whenToggled_thenEventIsEmitted() = runTest {
        launch {
            sut.toggleDrawer()
        }

        sut.events.test {
            val evt = awaitItem()
            assertEquals(DrawerEvent.Toggle, evt)
        }
    }

    @Test
    fun whenClosed_thenEventIsEmitted() = runTest {
        launch {
            sut.closeDrawer()
        }

        sut.events.test {
            val evt = awaitItem()
            assertEquals(DrawerEvent.Close, evt)
        }
    }

    @Test
    fun whenSetGesturesEnabled_thenStateIsUpdated() = runTest {
        val initial = sut.gesturesEnabled.value
        assertTrue(initial)

        sut.setGesturesEnabled(false)

        val value = sut.gesturesEnabled.value
        assertFalse(value)
    }

    @Test
    fun whenSendEvent_thenEventIsEmitted() = runTest {
        val community = CommunityModel(id = 0, name = "test")
        launch {
            sut.sendEvent(DrawerEvent.OpenCommunity(community))
        }
        sut.events.test {
            val evt = awaitItem()
            assertEquals(DrawerEvent.OpenCommunity(community), evt)
        }
    }
}
