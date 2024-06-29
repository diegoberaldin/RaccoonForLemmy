package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.SiteService
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultLemmyValueCacheTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val siteService = mockk<SiteService>()
    private val serviceProvider =
        mockk<ServiceProvider> {
            every { site } returns siteService
            every { currentInstance } returns "feddit.it"
        }
    private val sut =
        DefaultLemmyValueCache(
            services = serviceProvider,
        )

    @Test
    fun whenRefresh_thenInteractionsAreAsExpected() =
        runTest {
            val userId = 1L
            coEvery { siteService.get(any(), any()) } returns
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
                            every { moderates } returns listOf(mockk(relaxed = true))
                        }
                    every { admins } returns
                        listOf(
                            mockk(relaxed = true) {
                                every { person } returns
                                    mockk(relaxed = true) {
                                        every { id } returns userId
                                    }
                            },
                        )
                    every { siteView } returns
                        mockk(relaxed = true) {
                            every { localSite } returns
                                mockk(relaxed = true) {
                                    every { communityCreationAdminOnly } returns true
                                    every { enableDownvotes } returns false
                                }
                        }
                }

            sut.refresh(auth = AUTH_TOKEN)

            val isAdmin = sut.isCurrentUserAdmin.value
            val isCommunityCreationAdminOnly = sut.isCommunityCreationAdminOnly.value
            val isDownVoteEnabled = sut.isDownVoteEnabled.value
            val isCurrentUserModerator = sut.isCurrentUserModerator.value
            assertTrue(isAdmin)
            assertTrue(isCommunityCreationAdminOnly)
            assertFalse(isDownVoteEnabled)
            assertTrue(isCurrentUserModerator)

            coVerify {
                siteService.get(
                    auth = AUTH_TOKEN,
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                )
            }
        }

    companion object {
        private const val AUTH_TOKEN = "fake-auth-token"
    }
}
