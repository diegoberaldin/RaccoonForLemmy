package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.CommunitySortRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultLogoutUseCaseTest {

    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val identityRepository = mockk<IdentityRepository>(relaxUnitFun = true)
    private val accountRepository = mockk<AccountRepository>(relaxUnitFun = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxUnitFun = true)
    private val notificationCenter = mockk<NotificationCenter>(relaxUnitFun = true)
    private val communitySortRepository = mockk<CommunitySortRepository>(relaxUnitFun = true)
    private val sut = DefaultLogoutUseCase(
        identityRepository = identityRepository,
        accountRepository = accountRepository,
        settingsRepository = settingsRepository,
        notificationCenter = notificationCenter,
        communitySortRepository = communitySortRepository,
    )

    @Test
    fun whenExecute_thenInteractionsAreAsExpected() = runTest {
        val accountId = 1L
        coEvery {
            accountRepository.getActive()
        } returns AccountModel(
            id = accountId,
            instance = "fake-instance",
            username = "fake-username",
            jwt = "fake-token",
        )
        val anonymousSettings = SettingsModel()
        coEvery {
            settingsRepository.getSettings(any())
        } returns anonymousSettings

        sut()

        coVerify {
            notificationCenter.send(ofType(NotificationCenterEvent.ResetHome::class))
            notificationCenter.send(ofType(NotificationCenterEvent.ResetExplore::class))
            notificationCenter.send(ofType(NotificationCenterEvent.Logout::class))
            identityRepository.clearToken()
            accountRepository.setActive(accountId, false)
            settingsRepository.changeCurrentSettings(anonymousSettings)
        }
    }
}
