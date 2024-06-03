package com.github.diegoberaldin.raccoonforlemmy.feature.profile.main

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.testutils.MviModelTestRule
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged.ProfileNotLoggedMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged.ProfileNotLoggedViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertTrue

class ProfileNotLoggedViewModelTest {
    @get:Rule(order = 0)
    val dispatcherTestRule = DispatcherTestRule()

    @get:Rule(order = 1)
    val unloggedRule = MviModelTestRule {
        every { identityRepository.isLogged } returns MutableStateFlow(false)
        every { identityRepository.authToken } returns MutableStateFlow("")
        ProfileNotLoggedViewModel(
            identityRepository = identityRepository,
        )
    }

    @get:Rule(order = 1)
    val authErrorRule = MviModelTestRule {
        every { identityRepository.isLogged } returns MutableStateFlow(false)
        every { identityRepository.authToken } returns MutableStateFlow("fake-auth-token")
        ProfileNotLoggedViewModel(
            identityRepository = identityRepository,
        )
    }

    @get:Rule(order = 1)
    val loggedRule = MviModelTestRule {
        every { identityRepository.isLogged } returns MutableStateFlow(true)
        every { identityRepository.authToken } returns MutableStateFlow("fake-auth-token")
        ProfileNotLoggedViewModel(
            identityRepository = identityRepository,
        )
    }

    private val identityRepository = mockk<IdentityRepository>(relaxUnitFun = true)

    @Test
    fun givenNotLogged_whenInitialized_thenStateIsAsExpected() =
        runTest {
            with(unloggedRule) {
                onState { state ->
                    assertFalse(state.authError)
                }
            }
        }

    @Test
    fun givenTokenExpired_whenInitialized_thenStateIsAsExpected() =
        runTest {
            with(authErrorRule) {
                onState { state ->
                    assertTrue(state.authError)

                }
            }
        }

    @Test
    fun whenRetry_thenInteractionsAreAsExpected() =
        runTest {
            with(loggedRule) {
                onState { state ->
                    assertFalse(state.authError)
                }

                send(ProfileNotLoggedMviModel.Intent.Retry)

                coVerify {
                    identityRepository.refreshLoggedState()
                }
            }
        }
}
