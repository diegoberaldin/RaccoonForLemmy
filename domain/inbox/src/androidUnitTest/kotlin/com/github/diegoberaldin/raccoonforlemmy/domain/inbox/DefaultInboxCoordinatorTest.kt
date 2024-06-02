package com.github.diegoberaldin.raccoonforlemmy.domain.inbox

import app.cash.turbine.test
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.usecase.GetUnreadItemsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DefaultInboxCoordinatorTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val identityRepository =
        mockk<IdentityRepository>(relaxUnitFun = true) {
            every { isLogged } returns MutableStateFlow(true)
            every { authToken } returns MutableStateFlow("fake-token")
        }
    private val getUnreadItemsUseCase =
        mockk<GetUnreadItemsUseCase>(relaxUnitFun = true) {
            coEvery { getUnreadReplies() } returns 0
            coEvery { getUnreadMentions() } returns 0
            coEvery { getUnreadMessages() } returns 0
        }

    private val sut =
        DefaultInboxCoordinator(
            identityRepository = identityRepository,
            getUnreadItemsUseCase = getUnreadItemsUseCase,
            dispatcher = dispatcherTestRule.dispatcher,
        )

    @Test
    fun whenSetUnreadOnly_thenValueIsUpdated() =
        runTest {
            val resBefore = sut.unreadOnly.value
            sut.setUnreadOnly(!resBefore)

            val resAfter = sut.unreadOnly.value
            assertNotEquals(resBefore, resAfter)
        }

    @Test
    fun whenSendEvent_thenValueIsUpdated() =
        runTest {
            val evt = InboxCoordinator.Event.Refresh
            launch {
                sut.sendEvent(evt)
            }

            sut.events.test {
                val item = awaitItem()
                assertEquals(evt, item)
            }
        }
}
