package com.github.diegoberaldin.raccoonforlemmy.featrure.inbox.main

import app.cash.turbine.test
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InboxViewModelTest {

    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    private val identityRepository = mockk<IdentityRepository>(relaxUnitFun = true)
    private val userRepository = mockk<UserRepository>(relaxUnitFun = true)
    private val inboxCoordinator = mockk<InboxCoordinator>(relaxUnitFun = true) {
        every { unreadReplies } returns MutableStateFlow(0)
        every { unreadMentions } returns MutableStateFlow(0)
        every { unreadMessages } returns MutableStateFlow(0)
    }
    private val settingsRepository = mockk<SettingsRepository>(relaxUnitFun = true) {
        every { currentSettings } returns MutableStateFlow(SettingsModel())
    }
    private val notificationChannel = Channel<NotificationCenterEvent>()
    private val notificationCenter = mockk<NotificationCenter>(relaxUnitFun = true) {
        every { subscribe(any<KClass<NotificationCenterEvent>>()) } returns notificationChannel.receiveAsFlow()
    }
    private lateinit var sut: InboxViewModel

    private fun createModel() {
        sut = InboxViewModel(
            identityRepository = identityRepository,
            userRepository = userRepository,
            coordinator = inboxCoordinator,
            settingsRepository = settingsRepository,
            notificationCenter = notificationCenter,
        )
    }

    @Test
    fun givenNotLogged_whenInitialized_thenStateIsAsExpected() = runTest {
        every { identityRepository.isLogged } returns MutableStateFlow(false)
        createModel()

        val state = sut.uiState.value

        assertTrue(state.isLogged == false)
    }

    @Test
    fun givenLoggedAndDefaultUnreadOnly_whenInitialized_thenStateIsAsExpected() = runTest {
        every { identityRepository.isLogged } returns MutableStateFlow(true)
        every { settingsRepository.currentSettings } returns MutableStateFlow(SettingsModel(defaultInboxType = 0))
        createModel()

        val state = sut.uiState.value

        assertTrue(state.isLogged == true)
        assertTrue(state.unreadOnly)
    }

    @Test
    fun givenLoggedAndDefaultNotUnreadOnly_whenInitialized_thenStateIsAsExpected() = runTest {
        every { identityRepository.isLogged } returns MutableStateFlow(true)
        every { settingsRepository.currentSettings } returns MutableStateFlow(SettingsModel(defaultInboxType = 1))
        createModel()

        val state = sut.uiState.value

        assertTrue(state.isLogged == true)
        assertFalse(state.unreadOnly)
    }

    @Test
    fun givenLoggedWithUnreads_whenInitialized_thenStateIsAsExpected() = runTest {
        every { identityRepository.isLogged } returns MutableStateFlow(true)
        every { inboxCoordinator.unreadReplies } returns MutableStateFlow(1)
        every { inboxCoordinator.unreadMentions } returns MutableStateFlow(2)
        every { inboxCoordinator.unreadMessages } returns MutableStateFlow(3)
        createModel()

        val state = sut.uiState.value

        assertEquals(1, state.unreadReplies)
        assertEquals(2, state.unreadMentions)
        assertEquals(3, state.unreadMessages)
    }

    @Test
    fun whenChangeInboxReadOnlyEventReceived_thenInteractionsAndStateAreAsExpected() = runTest {
        every { identityRepository.isLogged } returns MutableStateFlow(true)
        every { identityRepository.authToken } returns MutableStateFlow("fake-token")
        createModel()

        notificationChannel.send(NotificationCenterEvent.ChangeInboxType(unreadOnly = false))

        val state = sut.uiState.value
        assertFalse(state.unreadOnly)

        verify {
            inboxCoordinator.setUnreadOnly(false)
        }
    }

    @Test
    fun whenMarkAllAsReadIntentReceived_thenInteractionsAreAsExpected() = runTest {
        every { identityRepository.isLogged } returns MutableStateFlow(true)
        every { identityRepository.authToken } returns MutableStateFlow("fake-token")
        createModel()

        launch {
            sut.reduce(InboxMviModel.Intent.ReadAll)
        }

        sut.effects.test {
            val item = awaitItem()
            assertEquals(InboxMviModel.Effect.Refresh, item)
        }

        coVerify {
            userRepository.readAll("fake-token")
            inboxCoordinator.sendEvent(InboxCoordinator.Event.Refresh)
        }
    }
}