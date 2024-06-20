package com.github.diegoberaldin.raccoonforlemmy.unit.drawer.cache

import app.cash.turbine.test
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommunityPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommunityPaginationSpecification
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DefaultSubscriptionsCacheTest {
    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    private val identityRepository = mockk<IdentityRepository>()
    private val communityPaginationManager = mockk<CommunityPaginationManager>()

    private val sut =
        DefaultSubscriptionsCache(
            identityRepository = identityRepository,
            communityPaginationManager = communityPaginationManager,
            dispatcher = dispatcherRule.dispatcher,
        )

    @Test
    fun whenInitial_thenStateIsAsExpected() =
        runTest {
            val state = sut.state.value

            assertEquals(SubscriptionsCacheState.Loading, state)
        }

    @Test
    fun whenInitialized_thenStateIsAsExpected() =
        runTest {
            val communityId = 1L
            every { identityRepository.isLogged } returns MutableStateFlow(true)
            coEvery {
                communityPaginationManager.reset(any())
            } returns Unit
            coEvery {
                communityPaginationManager.fetchAll()
            } returns listOf(CommunityModel(id = communityId))

            launch {
                delay(DELAY)
                sut.initialize()
            }

            sut.state.filterIsInstance<SubscriptionsCacheState.Loaded>().test {
                val item = awaitItem()
                assertEquals(1, item.communities.size)
                assertEquals(communityId, item.communities.first().id)
            }

            coVerify {
                communityPaginationManager.reset(
                    withArg {
                        assertIs<CommunityPaginationSpecification.Subscribed>(it)
                    },
                )
                communityPaginationManager.fetchAll()
            }
        }

    companion object {
        private const val DELAY = 250L
    }
}
