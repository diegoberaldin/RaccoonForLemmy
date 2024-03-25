package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.LoginResponse
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.CommunitySortRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.ApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.AuthRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DefaultLoginUseCaseTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()


    private val authRepository = mockk<AuthRepository>()
    private val apiConfigurationRepository = mockk<ApiConfigurationRepository>(relaxUnitFun = true) {
        every { instance } returns MutableStateFlow("")
    }
    private val identityRepository = mockk<IdentityRepository>(relaxUnitFun = true)
    private val accountRepository = mockk<AccountRepository>(relaxUnitFun = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxUnitFun = true)
    private val siteRepository = mockk<SiteRepository>(relaxUnitFun = true)
    private val communitySortRepository = mockk<CommunitySortRepository>(relaxUnitFun = true)
    private val sut = DefaultLoginUseCase(
        authRepository = authRepository,
        apiConfigurationRepository = apiConfigurationRepository,
        identityRepository = identityRepository,
        accountRepository = accountRepository,
        settingsRepository = settingsRepository,
        siteRepository = siteRepository,
        communitySortRepository = communitySortRepository,
    )

    @Test
    fun givenNewAccount_whenExecute_thenInteractionsAreAsExpected() = runTest {
        val accountId = 1L
        val anonymousSettings = SettingsModel()
        coEvery {
            authRepository.login(any(), any())
        } returns Result.success(LoginResponse("fake-token", registrationCreated = false, verifyEmailSent = true))
        coEvery {
            siteRepository.getAccountSettings(any())
        } returns null
        coEvery {
            accountRepository.getBy(any(), any())
        } returns null
        coEvery {
            accountRepository.getActive()
        } returns null
        coEvery {
            accountRepository.createAccount(any())
        } returns accountId
        coEvery {
            settingsRepository.getSettings(any())
        } returns anonymousSettings

        val res = sut("fake-instance", "fake-username", "fake-password")

        assertTrue(res.isSuccess)
        coVerify {
            apiConfigurationRepository.changeInstance("fake-instance")
            authRepository.login("fake-username", "fake-password")
            siteRepository.getAccountSettings("fake-token")
            accountRepository.getBy("fake-username", "fake-instance")
            accountRepository.createAccount(withArg {
                assertEquals("fake-username", it.username)
            })
            accountRepository.setActive(accountId, true)
            settingsRepository.createSettings(anonymousSettings, accountId)
            settingsRepository.changeCurrentSettings(anonymousSettings)
            communitySortRepository.clear()
        }
    }

    @Test
    fun givenPreviousDifferentAccount_whenExecute_thenInteractionsAreAsExpected() = runTest {
        val accountId = 2L
        val oldAccountId = 1L
        val anonymousSettings = SettingsModel()
        coEvery {
            authRepository.login(any(), any())
        } returns Result.success(LoginResponse("fake-token", registrationCreated = false, verifyEmailSent = true))
        coEvery {
            siteRepository.getAccountSettings(any())
        } returns null
        coEvery {
            accountRepository.getBy(any(), any())
        } returns null
        coEvery {
            accountRepository.getActive()
        } returns AccountModel(
            id = oldAccountId,
            username = "old-username",
            instance = "old-instance",
            jwt = "old-token"
        )
        coEvery {
            accountRepository.createAccount(any())
        } returns accountId
        coEvery {
            settingsRepository.getSettings(any())
        } returns anonymousSettings

        val res = sut("fake-instance", "fake-username", "fake-password")

        assertTrue(res.isSuccess)
        coVerify {
            apiConfigurationRepository.changeInstance("fake-instance")
            authRepository.login("fake-username", "fake-password")
            siteRepository.getAccountSettings("fake-token")
            accountRepository.getBy("fake-username", "fake-instance")
            accountRepository.createAccount(withArg {
                assertEquals("fake-username", it.username)
            })
            accountRepository.setActive(oldAccountId, false)
            accountRepository.setActive(accountId, true)
            settingsRepository.createSettings(anonymousSettings, accountId)
            settingsRepository.changeCurrentSettings(anonymousSettings)
            communitySortRepository.clear()
        }
    }

    @Test
    fun givenPreviousExistingAccount_whenExecute_thenInteractionsAreAsExpected() = runTest {
        val accountId = 2L
        val oldSettings = SettingsModel(id = 1)
        coEvery {
            authRepository.login(any(), any())
        } returns Result.success(LoginResponse("fake-token", registrationCreated = false, verifyEmailSent = true))
        coEvery {
            siteRepository.getAccountSettings(any())
        } returns null
        coEvery {
            accountRepository.getBy(any(), any())
        } returns AccountModel(
            id = accountId,
            username = "old-username",
            instance = "old-instance",
            jwt = "old-token"
        )
        coEvery {
            accountRepository.getActive()
        } returns null
        coEvery {
            accountRepository.createAccount(any())
        } returns accountId
        coEvery {
            settingsRepository.getSettings(accountId)
        } returns oldSettings

        val res = sut("fake-instance", "fake-username", "fake-password")

        assertTrue(res.isSuccess)
        coVerify {
            apiConfigurationRepository.changeInstance("fake-instance")
            authRepository.login("fake-username", "fake-password")
            siteRepository.getAccountSettings("fake-token")
            accountRepository.getBy("fake-username", "fake-instance")
            accountRepository.setActive(accountId, true)
            settingsRepository.changeCurrentSettings(oldSettings)
            communitySortRepository.clear()
        }
        coVerify(inverse = true) {
            accountRepository.createAccount(any())
            settingsRepository.createSettings(any(), any())
        }
    }
}