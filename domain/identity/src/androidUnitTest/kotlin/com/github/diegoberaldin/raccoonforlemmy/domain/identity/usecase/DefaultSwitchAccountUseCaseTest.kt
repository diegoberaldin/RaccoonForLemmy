package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
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

class DefaultSwitchAccountUseCaseTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()


    private val identityRepository = mockk<IdentityRepository>(relaxUnitFun = true)
    private val accountRepository = mockk<AccountRepository>(relaxUnitFun = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxUnitFun = true)
    private val serviceProvider = mockk<ServiceProvider>(relaxUnitFun = true)
    private val notificationCenter = mockk<NotificationCenter>(relaxUnitFun = true)
    private val communitySortRepository = mockk<CommunitySortRepository>(relaxUnitFun = true)
    private val sut = DefaultSwitchAccountUseCase(
        identityRepository = identityRepository,
        accountRepository = accountRepository,
        settingsRepository = settingsRepository,
        serviceProvider = serviceProvider,
        notificationCenter = notificationCenter,
        communitySortRepository = communitySortRepository,
    )

    @Test
    fun whenExecute_thenInteractionsAreAsExpected() = runTest {
        val accountId = 2L
        val oldAccountId = 1L
        val newAccount = AccountModel(
            id = accountId,
            username = "new-username",
            instance = "new-instance",
            jwt = "new-token"
        )
        val oldAccount = AccountModel(
            id = oldAccountId,
            username = "old-username",
            instance = "old-instance",
            jwt = "old-token"
        )
        val oldSettings = SettingsModel(id = 1)
        val newSettings = SettingsModel(id = 2)
        coEvery {
            accountRepository.getBy(any(), any())
        } returns null
        coEvery {
            accountRepository.getActive()
        } returns oldAccount
        coEvery {
            settingsRepository.getSettings(oldAccountId)
        } returns oldSettings
        coEvery {
            settingsRepository.getSettings(accountId)
        } returns newSettings

        sut(newAccount)

        coVerify {
            accountRepository.setActive(oldAccountId, false)
            accountRepository.setActive(accountId, true)
            settingsRepository.getSettings(accountId)
            settingsRepository.changeCurrentSettings(newSettings)
            communitySortRepository.clear()
            notificationCenter.send(ofType(NotificationCenterEvent.Logout::class))
            identityRepository.storeToken("new-token")
            identityRepository.refreshLoggedState()
            serviceProvider.changeInstance("new-instance")
        }
    }
}