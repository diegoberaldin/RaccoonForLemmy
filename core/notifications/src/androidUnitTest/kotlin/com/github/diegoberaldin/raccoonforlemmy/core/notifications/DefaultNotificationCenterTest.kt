package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import app.cash.turbine.test
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNotificationCenterTest {
    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    private val sut = DefaultNotificationCenter(
        dispatcher = dispatcherRule.dispatcher,
    )

    @Test
    fun givenSubscription_whenSendEvent_thenEventIsReceivedJustOnce() =
        runTest {
            launch {
                sut.send(NotificationCenterEvent.Logout)
            }

            sut.subscribe(NotificationCenterEvent.Logout::class).test {
                val evt = awaitItem()
                assertEquals(NotificationCenterEvent.Logout, evt)
            }

            sut.subscribe(NotificationCenterEvent.Logout::class).test {
                expectNoEvents()
            }
        }
}
