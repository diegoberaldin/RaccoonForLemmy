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

    private val sut = DefaultNotificationCenter

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

    @Test
    fun givenMultipleSubscriptions_whenSendReplayableEvent_thenEventIsReceivedAndReplayed() =
        runTest {
            launch {
                sut.send(NotificationCenterEvent.PostCreated)
            }

            sut.subscribe(NotificationCenterEvent.PostCreated::class).test {
                val evt = awaitItem()
                assertEquals(NotificationCenterEvent.PostCreated, evt)
            }

            sut.subscribe(NotificationCenterEvent.PostCreated::class).test {
                val evt = awaitItem()
                assertEquals(NotificationCenterEvent.PostCreated, evt)
            }
        }

    @Test
    fun givenMultipleSubscriptions_whenResetCache_thenEventIsNotReplayed() =
        runTest {
            launch {
                sut.send(NotificationCenterEvent.PostCreated)
            }
            sut.subscribe(NotificationCenterEvent.PostCreated::class).test {
                val evt = awaitItem()
                assertEquals(NotificationCenterEvent.PostCreated, evt)
            }

            sut.resetCache()

            sut.subscribe(NotificationCenterEvent.PostCreated::class).test {
                expectNoEvents()
            }
        }
}
