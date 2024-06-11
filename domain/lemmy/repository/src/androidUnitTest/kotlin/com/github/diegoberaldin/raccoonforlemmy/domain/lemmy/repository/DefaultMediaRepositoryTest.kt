package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.UserService
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.MediaModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultMediaRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val userService = mockk<UserService>()
    private val serviceProvider =
        mockk<ServiceProvider> {
            every { user } returns userService
        }
    private val sut =
        DefaultMediaRepository(
            services = serviceProvider,
        )

    @Test
    fun givenNoResults_whenGetAll_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                userService.listMedia(
                    authHeader = any(),
                    page = any(),
                    limit = any(),
                )
            } returns
                mockk {
                    every { images } returns emptyList()
                }

            val res =
                sut.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                )

            assertTrue(res.isEmpty())
            coVerify {
                userService.listMedia(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    page = 1,
                    limit = 20,
                )
            }
        }

    @Test
    fun givenResults_whenGetAll_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                userService.listMedia(
                    authHeader = any(),
                    page = any(),
                    limit = any(),
                )
            } returns
                mockk {
                    every { images } returns
                        listOf(
                            mockk(relaxed = true) {
                                every { localImage } returns
                                    mockk(relaxed = true) { every { pictrsAlias } returns "fake-alias" }
                            },
                        )
                }

            val res =
                sut.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                )

            assertEquals(1, res.size)
            assertEquals("fake-alias", res.first().alias)
            coVerify {
                userService.listMedia(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    page = 1,
                    limit = 20,
                )
            }
        }

    @Test
    fun whenDelete_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                userService.deleteImage(
                    authHeader = any(),
                    form = any(),
                )
            } returns true

            val media = MediaModel(alias = "fake-alias", deleteToken = "fake-delete-token")
            sut.delete(
                auth = AUTH_TOKEN,
                media = media,
            )

            coVerify {
                userService.deleteImage(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals("fake-delete-token", it.token)
                            assertEquals("fake-alias", it.filename)
                        },
                )
            }
        }

    companion object {
        private const val AUTH_TOKEN = "fake-auth-token"
    }
}
