package com.github.diegoberaldin.raccoonforlemmy.domain.identity.usecase

import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultDeleteAccountUseCaseTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()


    private val accountRepository = mockk<AccountRepository>(relaxUnitFun = true)
    private val sut = DefaultDeleteAccountUseCase(
        accountRepository = accountRepository,
    )

    @Test
    fun whenExecute_thenInteractionsAreAsExpected() = runTest {
        val account = AccountModel(id = 1, username = "test", jwt = "fake-token", instance = "test")
        sut(account)

        coVerify { accountRepository.delete(1) }
    }
}