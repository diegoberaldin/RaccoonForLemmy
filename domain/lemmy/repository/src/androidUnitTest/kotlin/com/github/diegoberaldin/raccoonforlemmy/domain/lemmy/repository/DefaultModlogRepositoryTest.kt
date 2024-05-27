package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetModlogResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.ModlogService
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItemType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DefaultModlogRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val modLogService: ModlogService = mockk()
    private val serviceProvider =
        mockk<ServiceProvider> {
            every { modLog } returns modLogService
        }
    private val sut = DefaultModlogRepository(
        services = serviceProvider,
    )

    @Test
    fun givenEmptyResponse_whenGetItems_thenResultAndInteractionsAreAsExpected() = runTest {
        coEvery {
            modLogService.getItems(
                authHeader = any(),
                auth = any(),
                page = any(),
                communityId = any(),
                limit = any(),
                modId = any(),
                otherId = any(),
                type = any()
            )
        } returns GetModlogResponse()

        val communityId = 1L
        val res = sut.getItems(
            auth = AUTH_TOKEN,
            communityId = communityId,
            page = 1,
            type = ModlogItemType.All,
        )

        assertNotNull(res)
        assertEquals(0, res.size)
        coVerify {
            modLogService.getItems(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                communityId = communityId,
                limit = 20,
                page = 1,
                type = ModlogItemType.All.toDto(),
            )
        }
    }

    @Test
    fun givenNonEmptyResponse_whenGetItems_thenResultAndInteractionsAreAsExpected() = runTest {
        coEvery {
            modLogService.getItems(
                authHeader = any(),
                auth = any(),
                page = any(),
                communityId = any(),
                limit = any(),
                modId = any(),
                otherId = any(),
                type = any()
            )
        } returns GetModlogResponse(
            removedComments = listOf(mockk(relaxed = true)),
            removedPosts = listOf(mockk(relaxed = true)),
        )

        val communityId = 1L
        val res = sut.getItems(
            auth = AUTH_TOKEN,
            communityId = communityId,
            page = 1,
        )

        assertNotNull(res)
        assertEquals(2, res.size)
        coVerify {
            modLogService.getItems(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                communityId = communityId,
                limit = 20,
                page = 1,
                type = ModlogItemType.All.toDto(),
            )
        }
    }

    companion object {
        private const val AUTH_TOKEN = "fake-token"
    }
}
