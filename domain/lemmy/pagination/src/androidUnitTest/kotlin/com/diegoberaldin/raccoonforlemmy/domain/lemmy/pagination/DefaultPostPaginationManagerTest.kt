package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DefaultPostPaginationManagerTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val identityRepository: IdentityRepository =
        mockk {
            every { authToken } returns MutableStateFlow(AUTH_TOKEN)
        }
    private val postRepository: PostRepository = mockk(relaxUnitFun = true)
    private val communityRepository: CommunityRepository = mockk(relaxUnitFun = true)
    private val userRepository: UserRepository = mockk(relaxUnitFun = true)
    private val multiCommunityPaginator: MultiCommunityPaginator = mockk(relaxUnitFun = true)
    private val notificationCenter: NotificationCenter = mockk(relaxUnitFun = true) {
        val slot = slot<KClass<NotificationCenterEvent>>()
        every { subscribe(capture(slot)) } answers { MutableSharedFlow() }
    }

    private val sut =
        DefaultPostPaginationManager(
            identityRepository = identityRepository,
            postRepository = postRepository,
            communityRepository = communityRepository,
            userRepository = userRepository,
            multiCommunityPaginator = multiCommunityPaginator,
            notificationCenter = notificationCenter,
        )

    @Test
    fun whenReset_thenHistoryIsClearedAndCanFetchMore() =
        runTest {
            val specification = PostPaginationSpecification.Listing()
            sut.reset(specification)

            assertTrue(sut.canFetchMore)
            assertTrue(sut.history.isEmpty())
        }

    @Test
    fun givenListingSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                postRepository.getAll(
                    auth = any(),
                    page = any(),
                    pageCursor = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    communityId = any(),
                    communityName = any(),
                    otherInstance = any(),
                )
            } returns (emptyList<PostModel>() to null)
            val specification = PostPaginationSpecification.Listing()
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    type = specification.listingType,
                    sort = specification.sortType,
                    communityId = null,
                    communityName = null,
                )
            }
        }

    @Test
    fun givenListingSpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                postRepository.getAll(
                    auth = any(),
                    page = capture(page),
                    pageCursor = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    communityId = any(),
                    communityName = any(),
                    otherInstance = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    } to PAGE_CURSOR
                } else {
                    (emptyList<PostModel>() to null)
                }
            }
            val specification = PostPaginationSpecification.Listing()
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    type = specification.listingType,
                    sort = specification.sortType,
                    communityId = null,
                    communityName = null,
                )
            }
        }

    @Test
    fun givenListingSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                postRepository.getAll(
                    auth = any(),
                    page = capture(page),
                    pageCursor = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    communityId = any(),
                    communityName = any(),
                    otherInstance = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    } to PAGE_CURSOR
                } else {
                    (emptyList<PostModel>() to null)
                }
            }
            val specification = PostPaginationSpecification.Listing()
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    type = specification.listingType,
                    sort = specification.sortType,
                    communityId = null,
                    communityName = null,
                )
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 2,
                    pageCursor = PAGE_CURSOR,
                    limit = 20,
                    type = specification.listingType,
                    sort = specification.sortType,
                    communityId = null,
                    communityName = null,
                )
            }
        }

    @Test
    fun givenMultiCommunitySpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                multiCommunityPaginator.loadNextPage(any(), any())
            } returns emptyList()
            every { multiCommunityPaginator.canFetchMore } returns false
            val communityIds = listOf(1L)
            val specification = PostPaginationSpecification.MultiCommunity(communityIds = communityIds)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                multiCommunityPaginator.setCommunities(communityIds)
                multiCommunityPaginator.loadNextPage(
                    auth = AUTH_TOKEN,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenMultiCommunitySpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                multiCommunityPaginator.loadNextPage(any(), any())
            } answers {
                (0..<20).map { idx ->
                    PostModel(id = idx.toLong())
                }
            }
            every { multiCommunityPaginator.canFetchMore } returns true
            val communityIds = listOf(1L)
            val specification = PostPaginationSpecification.MultiCommunity(communityIds = communityIds)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                multiCommunityPaginator.setCommunities(communityIds)
                multiCommunityPaginator.loadNextPage(
                    auth = AUTH_TOKEN,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenMultiCommunitySpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            var invokeCount = 0
            coEvery {
                multiCommunityPaginator.loadNextPage(any(), any())
            } answers {
                val res =
                    if (invokeCount == 0) {
                        (0..<20).map { idx ->
                            PostModel(id = idx.toLong())
                        }
                    } else {
                        emptyList()
                    }
                invokeCount++
                res
            }
            every { multiCommunityPaginator.canFetchMore } answers {
                invokeCount == 0
            }
            val communityIds = listOf(1L)
            val specification = PostPaginationSpecification.MultiCommunity(communityIds = communityIds)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerify {
                multiCommunityPaginator.setCommunities(communityIds)
            }
            coVerify(exactly = 2) {
                multiCommunityPaginator.loadNextPage(
                    auth = AUTH_TOKEN,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenCommunitySpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                postRepository.getAll(
                    auth = any(),
                    page = any(),
                    pageCursor = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    communityId = any(),
                    communityName = any(),
                    otherInstance = any(),
                )
            } returns (emptyList<PostModel>() to null)
            val communityId = 1L
            val specification = PostPaginationSpecification.Community(communityId)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    sort = specification.sortType,
                    communityId = communityId,
                )
            }
        }

    @Test
    fun givenCommunitySpecSearchingAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                communityRepository.search(
                    auth = any(),
                    communityId = any(),
                    page = any(),
                    sortType = any(),
                    resultType = any(),
                    query = any(),
                )
            } returns emptyList()
            val communityId = 1L
            val searchText = "query"
            val specification = PostPaginationSpecification.Community(id = communityId, query = searchText)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    communityId = communityId,
                    page = 1,
                    sortType = specification.sortType,
                    resultType = SearchResultType.Posts,
                    query = searchText,
                )
            }
        }

    @Test
    fun givenCommunitySpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                postRepository.getAll(
                    auth = any(),
                    page = capture(page),
                    pageCursor = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    communityId = any(),
                    communityName = any(),
                    otherInstance = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    } to PAGE_CURSOR
                } else {
                    emptyList<PostModel>() to null
                }
            }
            val communityId = 1L
            val specification = PostPaginationSpecification.Community(communityId)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    sort = specification.sortType,
                    communityId = communityId,
                )
            }
        }

    @Test
    fun givenCommunitySpecSearchingAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                communityRepository.search(
                    auth = any(),
                    communityId = any(),
                    page = capture(page),
                    sortType = any(),
                    resultType = any(),
                    query = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        SearchResult.Post(
                            model = PostModel(id = idx.toLong()),
                        )
                    }
                } else {
                    emptyList()
                }
            }
            val communityId = 1L
            val searchText = "query"
            val specification = PostPaginationSpecification.Community(id = communityId, query = searchText)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    communityId = communityId,
                    page = 1,
                    sortType = specification.sortType,
                    resultType = SearchResultType.Posts,
                    query = searchText,
                )
            }
        }

    @Test
    fun givenCommunitySpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                postRepository.getAll(
                    auth = any(),
                    page = capture(page),
                    pageCursor = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    communityId = any(),
                    communityName = any(),
                    otherInstance = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    } to PAGE_CURSOR
                } else {
                    emptyList<PostModel>() to null
                }
            }
            val communityId = 1L
            val specification = PostPaginationSpecification.Community(communityId)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    sort = specification.sortType,
                    communityId = communityId,
                )
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 2,
                    pageCursor = PAGE_CURSOR,
                    limit = 20,
                    sort = specification.sortType,
                    communityId = communityId,
                )
            }
        }

    @Test
    fun givenCommunitySpecSearchingAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                communityRepository.search(
                    auth = any(),
                    communityId = any(),
                    page = capture(page),
                    sortType = any(),
                    resultType = any(),
                    query = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        SearchResult.Post(
                            model = PostModel(id = idx.toLong()),
                        )
                    }
                } else {
                    emptyList()
                }
            }
            val communityId = 1L
            val searchText = "query"
            val specification = PostPaginationSpecification.Community(id = communityId, query = searchText)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    communityId = communityId,
                    page = 1,
                    sortType = specification.sortType,
                    resultType = SearchResultType.Posts,
                    query = searchText,
                )
                communityRepository.search(
                    auth = AUTH_TOKEN,
                    communityId = communityId,
                    page = 2,
                    sortType = specification.sortType,
                    resultType = SearchResultType.Posts,
                    query = searchText,
                )
            }
        }

    @Test
    fun givenUserSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val userId = 1L
            coEvery {
                userRepository.getPosts(
                    id = userId,
                    auth = any(),
                    page = any(),
                    limit = any(),
                    sort = any(),
                    otherInstance = any(),
                )
            } returns emptyList()
            val specification = PostPaginationSpecification.User(userId)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                userRepository.getPosts(
                    id = userId,
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenUserSpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val userId = 1L
            val page = slot<Int>()
            coEvery {
                userRepository.getPosts(
                    id = userId,
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    sort = any(),
                    otherInstance = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    }
                } else {
                    emptyList()
                }
            }
            val specification = PostPaginationSpecification.User(userId)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                userRepository.getPosts(
                    id = userId,
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenUserSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val userId = 1L
            val page = slot<Int>()
            coEvery {
                userRepository.getPosts(
                    id = userId,
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    sort = any(),
                    otherInstance = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    }
                } else {
                    emptyList()
                }
            }
            val specification = PostPaginationSpecification.User(userId)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                userRepository.getPosts(
                    id = userId,
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    sort = specification.sortType,
                )
                userRepository.getPosts(
                    id = userId,
                    auth = AUTH_TOKEN,
                    page = 2,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenVotesSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                userRepository.getLikedPosts(
                    auth = any(),
                    page = any(),
                    limit = any(),
                    sort = any(),
                    liked = any(),
                    pageCursor = any(),
                )
            } returns (emptyList<PostModel>() to null)
            val specification = PostPaginationSpecification.Votes(liked = true)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                userRepository.getLikedPosts(
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    liked = specification.liked,
                    sort = specification.sortType,
                    pageCursor = null,
                )
            }
        }

    @Test
    fun givenVotesSpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                userRepository.getLikedPosts(
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    sort = any(),
                    liked = any(),
                    pageCursor = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    } to PAGE_CURSOR
                } else {
                    emptyList<PostModel>() to null
                }
            }
            val specification = PostPaginationSpecification.Votes(liked = true)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                userRepository.getLikedPosts(
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    liked = specification.liked,
                    sort = specification.sortType,
                    pageCursor = null,
                )
            }
        }

    @Test
    fun givenVotesSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                userRepository.getLikedPosts(
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    sort = any(),
                    liked = any(),
                    pageCursor = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    } to PAGE_CURSOR
                } else {
                    emptyList<PostModel>() to null
                }
            }
            val specification = PostPaginationSpecification.Votes(liked = true)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                userRepository.getLikedPosts(
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    liked = specification.liked,
                    sort = specification.sortType,
                    pageCursor = null,
                )
                userRepository.getLikedPosts(
                    auth = AUTH_TOKEN,
                    page = 2,
                    limit = 20,
                    liked = specification.liked,
                    sort = specification.sortType,
                    pageCursor = PAGE_CURSOR,
                )
            }
        }

    @Test
    fun givenSavedSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val userId = 1L
            coEvery { identityRepository.cachedUser } returns UserModel(id = userId)
            coEvery {
                userRepository.getSavedPosts(
                    id = any(),
                    auth = any(),
                    page = any(),
                    limit = any(),
                    sort = any(),
                )
            } returns emptyList()
            val specification = PostPaginationSpecification.Saved()
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                userRepository.getSavedPosts(
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    id = userId,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenSavedSpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val userId = 1L
            coEvery { identityRepository.cachedUser } returns UserModel(id = userId)
            val page = slot<Int>()
            coEvery {
                userRepository.getSavedPosts(
                    id = any(),
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    sort = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    }
                } else {
                    emptyList()
                }
            }
            val specification = PostPaginationSpecification.Saved()
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                userRepository.getSavedPosts(
                    id = userId,
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenSavedSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val userId = 1L
            coEvery { identityRepository.cachedUser } returns UserModel(id = userId)
            val page = slot<Int>()
            coEvery {
                userRepository.getSavedPosts(
                    id = any(),
                    auth = any(),
                    page = capture(page),
                    limit = any(),
                    sort = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        PostModel(id = idx.toLong())
                    }
                } else {
                    emptyList()
                }
            }
            val specification = PostPaginationSpecification.Saved()
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                userRepository.getSavedPosts(
                    id = userId,
                    auth = AUTH_TOKEN,
                    page = 1,
                    limit = 20,
                    sort = specification.sortType,
                )
                userRepository.getSavedPosts(
                    id = userId,
                    auth = AUTH_TOKEN,
                    page = 2,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun whenExtractAndRestoreState_thenResultIsAsExpected() {
        val state = sut.extractState()

        sut.restoreState(state)

        val res = sut.extractState()
        assertEquals(state, res)
    }

    companion object {
        private const val AUTH_TOKEN = "fake-token"
        private const val PAGE_CURSOR = "fake-page-cursor"
    }
}
