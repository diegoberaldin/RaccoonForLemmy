package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DefaultCommunityPaginationManagerTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val identityRepository: IdentityRepository =
        mockk {
            every { authToken } returns MutableStateFlow(AUTH_TOKEN)
        }
    private val communityRepository: CommunityRepository = mockk(relaxUnitFun = true)

    private val sut =
        DefaultCommunityPaginationManager(
            identityRepository = identityRepository,
            communityRepository = communityRepository,
        )

    @Test
    fun whenReset_thenHistoryIsClearedAndCanFetchMore() =
        runTest {
            val specification = CommunityPaginationSpecification.Subscribed()
            sut.reset(specification)

            assertTrue(sut.canFetchMore)
            assertTrue(sut.history.isEmpty())
        }

    @Test
    fun givenSubscribedSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                communityRepository.search(
                    auth = any(),
                    page = any(),
                    limit = any(),
                    resultType = any(),
                    sortType = any(),
                    query = any(),
                    communityId = any(),
                    instance = any(),
                    listingType = any(),
                )
            } returns emptyList()
            val specification = CommunityPaginationSpecification.Subscribed()
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Subscribed,
                )
            }
        }

    @Test
    fun givenSubscribedSpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                communityRepository.search(
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    resultType = any(),
                    sortType = any(),
                    query = any(),
                    communityId = any(),
                    instance = any(),
                    listingType = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        SearchResult.Community(model = CommunityModel(id = idx.toLong()))
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommunityPaginationSpecification.Subscribed()
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Subscribed,
                )
            }
        }

    @Test
    fun givenSubscribedSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                communityRepository.search(
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    resultType = any(),
                    sortType = any(),
                    query = any(),
                    communityId = any(),
                    instance = any(),
                    listingType = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        SearchResult.Community(model = CommunityModel(id = idx.toLong()))
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommunityPaginationSpecification.Subscribed()
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Subscribed,
                )
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    page = 2,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Subscribed,
                )
            }
        }

    @Test
    fun givenSubscribedSpec_whenFetchAll_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                communityRepository.search(
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    resultType = any(),
                    sortType = any(),
                    query = any(),
                    communityId = any(),
                    instance = any(),
                    listingType = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        SearchResult.Community(model = CommunityModel(id = idx.toLong()))
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommunityPaginationSpecification.Subscribed()
            sut.reset(specification)

            val items = sut.fetchAll()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Subscribed,
                )
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    page = 2,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Subscribed,
                )
            }
        }

    @Test
    fun givenInstanceSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                communityRepository.search(
                    auth = any(),
                    page = any(),
                    limit = any(),
                    resultType = any(),
                    sortType = any(),
                    query = any(),
                    communityId = any(),
                    instance = any(),
                    listingType = any(),
                )
            } returns emptyList()
            val otherInstance = "fake-instance"
            val specification =
                CommunityPaginationSpecification.Instance(otherInstance = otherInstance)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                communityRepository.search(
                    page = 1,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Local,
                    instance = otherInstance,
                )
            }
        }

    @Test
    fun givenInstanceSpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                communityRepository.search(
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    resultType = any(),
                    sortType = any(),
                    query = any(),
                    communityId = any(),
                    instance = any(),
                    listingType = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        SearchResult.Community(model = CommunityModel(id = idx.toLong()))
                    }
                } else {
                    emptyList()
                }
            }
            val otherInstance = "fake-instance"
            val specification =
                CommunityPaginationSpecification.Instance(otherInstance = otherInstance)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                communityRepository.search(
                    instance = otherInstance,
                    page = 1,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Local,
                )
            }
        }

    @Test
    fun givenInstanceSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                communityRepository.search(
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    resultType = any(),
                    sortType = any(),
                    query = any(),
                    communityId = any(),
                    instance = any(),
                    listingType = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        SearchResult.Community(model = CommunityModel(id = idx.toLong()))
                    }
                } else {
                    emptyList()
                }
            }
            val otherInstance = "fake-instance"
            val specification =
                CommunityPaginationSpecification.Instance(otherInstance = otherInstance)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                communityRepository.search(
                    instance = otherInstance,
                    page = 1,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Local,
                )
                communityRepository.search(
                    instance = otherInstance,
                    page = 2,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Local,
                )
            }
        }

    @Test
    fun givenInstanceSpec_whenFetchAll_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                communityRepository.search(
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    resultType = any(),
                    sortType = any(),
                    query = any(),
                    communityId = any(),
                    instance = any(),
                    listingType = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        SearchResult.Community(model = CommunityModel(id = idx.toLong()))
                    }
                } else {
                    emptyList()
                }
            }
            val otherInstance = "fake-instance"
            val specification =
                CommunityPaginationSpecification.Instance(otherInstance = otherInstance)
            sut.reset(specification)

            val items = sut.fetchAll()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                communityRepository.search(
                    instance = otherInstance,
                    page = 1,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Local,
                )
                communityRepository.search(
                    instance = otherInstance,
                    page = 2,
                    limit = 50,
                    resultType = SearchResultType.Communities,
                    sortType = specification.sortType,
                    listingType = ListingType.Local,
                )
            }
        }

    companion object {
        private const val AUTH_TOKEN = "fake-token"
    }
}
