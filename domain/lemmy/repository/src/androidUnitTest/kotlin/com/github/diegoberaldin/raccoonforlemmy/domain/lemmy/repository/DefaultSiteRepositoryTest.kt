package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockInstanceForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.BlockInstanceResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.Language
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SiteMetadata
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.PostService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.SiteService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.UserService
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.AccountSettingsModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DefaultSiteRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val siteService = mockk<SiteService>()
    private val postService = mockk<PostService>()
    private val userService = mockk<UserService>()
    private val serviceProvider =
        mockk<ServiceProvider> {
            every { site } returns siteService
            every { post } returns postService
            every { user } returns userService
            every { currentInstance } returns "feddit.it"
        }
    private val customServiceProvider =
        mockk<ServiceProvider> {
            every { site } returns siteService
            every { post } returns postService
            every { currentInstance } returns "lemmy.ml"
        }

    private val sut =
        DefaultSiteRepository(
            services = serviceProvider,
            customServices = customServiceProvider,
        )

    @Test
    fun whenGetCurrentUser_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val userId = 1L
            coEvery {
                siteService.get(auth = any(), authHeader = any())
            } returns
                mockk(relaxed = true) {
                    every { myUser } returns
                        mockk(relaxed = true) {
                            every { localUserView } returns
                                mockk(relaxed = true) {
                                    every { person } returns
                                        mockk(relaxed = true) {
                                            every { id } returns userId
                                        }
                                }
                        }
                }

            val res = sut.getCurrentUser(auth = AUTH_TOKEN)

            assertNotNull(res)
            assertEquals(userId, res.id)
            coVerify {
                siteService.get(
                    auth = AUTH_TOKEN,
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                )
            }
        }

    @Test
    fun whenGetSiteVersion_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val versionValue = "0.19.1"
            coEvery {
                siteService.get(auth = any(), authHeader = any())
            } returns
                mockk {
                    every { version } returns versionValue
                }

            val res = sut.getSiteVersion()

            assertEquals(versionValue, res)
            coVerify {
                siteService.get()
            }
        }

    @Test
    fun whenBlock_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val instanceId = 1L
            val formSlot = slot<BlockInstanceForm>()
            coEvery {
                siteService.block(authHeader = any(), form = capture(formSlot))
            } answers {
                BlockInstanceResponse(blocked = formSlot.captured.block)
            }

            sut.block(
                auth = AUTH_TOKEN,
                id = instanceId,
                blocked = true,
            )

            coVerify {
                siteService.block(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertTrue(it.block)
                            assertEquals(instanceId, it.instanceId)
                        },
                )
            }
        }

    @Test
    fun whenGetMetadata_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val instanceTitle = "feddit.it"
            val siteMetadata = SiteMetadata(title = instanceTitle)
            coEvery {
                postService.getSiteMetadata(any(), any())
            } returns
                mockk {
                    every { metadata } returns siteMetadata
                }

            val res = sut.getMetadata("https://$instanceTitle")

            assertNotNull(res)
            assertEquals(instanceTitle, res.title)
            coVerify {
                postService.getSiteMetadata(
                    url = "https://$instanceTitle",
                )
            }
        }

    @Test
    fun whenGetLanguages_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val languages = listOf(Language(id = 0, code = "en", name = "English"))
            coEvery {
                siteService.get(auth = any(), authHeader = any())
            } returns
                mockk {
                    every { allLanguages } returns languages
                }

            val res = sut.getLanguages(auth = AUTH_TOKEN)

            assertEquals(1, res.size)
            assertEquals(0L, res.first().id)
            assertEquals("en", res.first().code)
            coVerify {
                siteService.get(
                    auth = AUTH_TOKEN,
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                )
            }
        }

    @Test
    fun whenGetAccountSettings_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val personBio = "fake-bio"
            coEvery {
                siteService.get(auth = any(), authHeader = any())
            } returns
                mockk(relaxed = true) {
                    every { myUser } returns
                        mockk(relaxed = true) {
                            every { localUserView } returns
                                mockk(relaxed = true) {
                                    every { localUser } returns
                                        mockk(relaxed = true) {
                                            every { person } returns
                                                mockk(relaxed = true) {
                                                    every { bio } returns personBio
                                                }
                                        }
                                }
                        }
                }

            val res = sut.getAccountSettings(auth = AUTH_TOKEN)

            assertNotNull(res)
            assertEquals(personBio, res.bio)
            coVerify {
                siteService.get(
                    auth = AUTH_TOKEN,
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                )
            }
        }

    @Test
    fun whenUpdateAccountSettings_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val personBio = "fake-bio"
            coEvery {
                userService.saveUserSettings(authHeader = any(), form = any())
            } returns mockk(relaxed = true)

            sut.updateAccountSettings(
                auth = AUTH_TOKEN,
                value = AccountSettingsModel(bio = personBio),
            )

            coVerify {
                userService.saveUserSettings(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(personBio, it.bio)
                        },
                )
            }
        }

    @Test
    fun whenGetBans_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                siteService.get(auth = any(), authHeader = any())
            } returns
                mockk(relaxed = true) {
                    every { myUser } returns
                        mockk(relaxed = true) {
                            every { personBlocks } returns
                                listOf(
                                    mockk(relaxed = true) {
                                        every { target } returns mockk(relaxed = true) { every { id } returns 1L }
                                    },
                                )
                            every { communityBlocks } returns
                                listOf(
                                    mockk(relaxed = true) {
                                        every { community } returns mockk(relaxed = true) { every { id } returns 2L }
                                    },
                                )
                            every { instanceBlocks } returns
                                listOf(
                                    mockk(relaxed = true) {
                                        every { instance } returns mockk(relaxed = true) { every { id } returns 3L }
                                    },
                                )
                        }
                }
            val res = sut.getBans(auth = AUTH_TOKEN)

            assertNotNull(res)
            assertTrue(res.users.isNotEmpty())
            assertEquals(1L, res.users.first().id)
            assertTrue(res.communities.isNotEmpty())
            assertEquals(2L, res.communities.first().id)
            assertTrue(res.instances.isNotEmpty())
            assertEquals(3L, res.instances.first().id)
            coVerify {
                siteService.get(
                    auth = AUTH_TOKEN,
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                )
            }
        }

    @Test
    fun whenGetAdmins_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                siteService.get(auth = any(), authHeader = any())
            } returns
                mockk {
                    every { admins } returns
                        listOf(
                            mockk(relaxed = true) {
                                every { person } returns
                                    mockk(relaxed = true) {
                                        every { id } returns 1L
                                    }
                            },
                        )
                }

            val res = sut.getAdmins()

            assertTrue(res.isNotEmpty())
            assertEquals(1L, res.first().id)
        }

    companion object {
        private const val AUTH_TOKEN = "fake-auth-token"
    }
}
