package com.github.diegoberaldin.raccoonforlemmy.featrure.inbox.main

import app.cash.turbine.test
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.testutils.MviModelTestRule
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxSection
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
    @get:Rule(order = 0)
    val dispatcherRule = DispatcherTestRule()

    @get:Rule(order = 1)
    val unloggedRuleRule =
        MviModelTestRule {
            every { identityRepository.isLogged } returns MutableStateFlow(false)

            InboxViewModel(
                identityRepository = identityRepository,
                userRepository = userRepository,
                coordinator = inboxCoordinator,
                settingsRepository = settingsRepository,
                notificationCenter = notificationCenter,
            )
        }

    @get:Rule(order = 1)
    val loggedDefaultUnreadOnlyRule =
        MviModelTestRule {
            every { identityRepository.isLogged } returns MutableStateFlow(true)
            every { identityRepository.authToken } returns MutableStateFlow(AUTH_TOKEN)
            every { settingsRepository.currentSettings } returns
                MutableStateFlow(
                    SettingsModel(defaultInboxType = 0),
                )

            InboxViewModel(
                identityRepository = identityRepository,
                userRepository = userRepository,
                coordinator = inboxCoordinator,
                settingsRepository = settingsRepository,
                notificationCenter = notificationCenter,
            )
        }

    @get:Rule(order = 1)
    val loggedDefaultAllRule =
        MviModelTestRule {
            every { identityRepository.isLogged } returns MutableStateFlow(true)
            every { identityRepository.authToken } returns MutableStateFlow(AUTH_TOKEN)
            every { settingsRepository.currentSettings } returns
                MutableStateFlow(
                    SettingsModel(defaultInboxType = 1),
                )

            InboxViewModel(
                identityRepository = identityRepository,
                userRepository = userRepository,
                coordinator = inboxCoordinator,
                settingsRepository = settingsRepository,
                notificationCenter = notificationCenter,
            )
        }

    @get:Rule(order = 1)
    val loggedWithUnreadsRule =
        MviModelTestRule {
            every { identityRepository.isLogged } returns MutableStateFlow(true)
            every { identityRepository.authToken } returns MutableStateFlow(AUTH_TOKEN)
            every { settingsRepository.currentSettings } returns
                MutableStateFlow(
                    SettingsModel(defaultInboxType = 1),
                )
            every { inboxCoordinator.unreadReplies } returns MutableStateFlow(1)
            every { inboxCoordinator.unreadMentions } returns MutableStateFlow(2)
            every { inboxCoordinator.unreadMessages } returns MutableStateFlow(3)

            InboxViewModel(
                identityRepository = identityRepository,
                userRepository = userRepository,
                coordinator = inboxCoordinator,
                settingsRepository = settingsRepository,
                notificationCenter = notificationCenter,
            )
        }

    private val identityRepository = mockk<IdentityRepository>(relaxUnitFun = true)
    private val userRepository = mockk<UserRepository>(relaxUnitFun = true)
    private val inboxCoordinator =
        mockk<InboxCoordinator>(relaxUnitFun = true) {
            every { unreadReplies } returns MutableStateFlow(0)
            every { unreadMentions } returns MutableStateFlow(0)
            every { unreadMessages } returns MutableStateFlow(0)
        }
    private val settingsRepository =
        mockk<SettingsRepository>(relaxUnitFun = true) {
            every { currentSettings } returns MutableStateFlow(SettingsModel())
        }
    private val notificationChannel = Channel<NotificationCenterEvent>()
    private val notificationCenter =
        mockk<NotificationCenter>(relaxUnitFun = true) {
            every { subscribe(any<KClass<NotificationCenterEvent>>()) } returns notificationChannel.receiveAsFlow()
        }

    @Test
    fun givenNotLogged_whenInitialized_thenStateIsAsExpected() =
        runTest {
            with(unloggedRuleRule) {
                onState { state ->
                    assertEquals(false, state.isLogged)
                }
            }
        }

    @Test
    fun givenLoggedAndDefaultUnreadOnly_whenInitialized_thenStateIsAsExpected() =
        runTest {
            with(loggedDefaultUnreadOnlyRule) {
                onState { state ->
                    assertTrue(state.isLogged == true)
                    assertTrue(state.unreadOnly)
                }
            }
        }

    @Test
    fun givenLoggedAndDefaultNotUnreadOnly_whenInitialized_thenStateIsAsExpected() =
        runTest {
            with(loggedDefaultAllRule) {
                onState { state ->
                    assertTrue(state.isLogged == true)
                    assertFalse(state.unreadOnly)
                }
            }
        }

    @Test
    fun givenLoggedWithUnreads_whenInitialized_thenStateIsAsExpected() =
        runTest {
            with(loggedWithUnreadsRule) {
                onState { state ->
                    assertTrue(state.isLogged == true)
                    assertEquals(1, state.unreadReplies)
                    assertEquals(2, state.unreadMentions)
                    assertEquals(3, state.unreadMessages)
                }
            }
        }

    @Test
    fun whenChangeInboxReadOnlyEventReceived_thenInteractionsAndStateAreAsExpected() =
        runTest {
            with(loggedDefaultUnreadOnlyRule) {
                notificationChannel.send(NotificationCenterEvent.ChangeInboxType(unreadOnly = false))

                onState { state ->
                    assertFalse(state.unreadOnly)
                }

                verify {
                    inboxCoordinator.setUnreadOnly(false)
                }
            }
        }

    @Test
    fun whenMarkAllAsReadIntentReceived_thenInteractionsAreAsExpected() =
        runTest {
            with(loggedWithUnreadsRule) {
                every { inboxCoordinator.totalUnread } returns MutableStateFlow(1)

                launch {
                    send(InboxMviModel.Intent.ReadAll)
                }

                onEffects { effects ->
                    effects.test {
                        val item = awaitItem()
                        assertEquals(InboxMviModel.Effect.Refresh, item)
                        val item2 = awaitItem()
                        assertEquals(InboxMviModel.Effect.ReadAllInboxSuccess, item2)
                    }

                    coVerify {
                        userRepository.readAll(AUTH_TOKEN)
                        inboxCoordinator.sendEvent(InboxCoordinator.Event.Refresh)
                    }
                }
            }
        }

    @Test
    fun whenChangeSectionIntentReceived_thenStateIsAsExpected() =
        runTest {
            with(loggedDefaultAllRule) {
                val section = InboxSection.Mentions
                send(InboxMviModel.Intent.ChangeSection(section))

                onState { state ->
                    assertEquals(section, state.section)
                }
            }
        }

    companion object {
        private const val AUTH_TOKEN = "fake-token"
    }
}
