package com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.LoginForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.LoginResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.AuthService
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import de.jensklingenberg.ktorfit.Response
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DefaultAuthRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val authService = mockk<AuthService>()
    private val serviceProvider =
        mockk<ServiceProvider>(relaxUnitFun = true) {
            every { auth } returns authService
        }

    private val sut =
        DefaultAuthRepository(
            services = serviceProvider,
        )

    @Test
    fun whenLogin_thenResultIsAsExpected() =
        runTest {
            val loginData = LoginResponse(token = "", registrationCreated = false, verifyEmailSent = true)
            val fakeResponse =
                mockk<Response<LoginResponse>> {
                    every { isSuccessful } returns true
                    every { body() } returns loginData
                }
            coEvery { authService.login(any()) } returns fakeResponse
            val res = sut.login("username", "password")

            assertTrue(res.isSuccess)

            val resultData = res.getOrThrow()
            assertEquals(loginData, resultData)
            coVerify {
                authService.login(LoginForm("username", "password"))
            }
        }
}
