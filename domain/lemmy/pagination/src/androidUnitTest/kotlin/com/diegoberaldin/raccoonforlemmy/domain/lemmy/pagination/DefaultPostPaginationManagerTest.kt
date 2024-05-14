package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
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

class DefaultPostPaginationManagerTest {

    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val identityRepository: IdentityRepository = mockk {
        every { authToken } returns MutableStateFlow("fake-token")
    }
    private val postRepository: PostRepository = mockk(relaxUnitFun = true)
    private val communityRepository: CommunityRepository = mockk(relaxUnitFun = true)
    private val userRepository: UserRepository = mockk(relaxUnitFun = true)
    private val multiCommunityPaginator: MultiCommunityPaginator = mockk(relaxUnitFun = true)

    private val sut = DefaultPostPaginationManager(
        identityRepository = identityRepository,
        postRepository = postRepository,
        communityRepository = communityRepository,
        userRepository = userRepository,
        multiCommunityPaginator = multiCommunityPaginator,
    )

    @Test
    fun whenReset_thenHistoryIsClearedAndCanFetchMore() = runTest {
        val specification = PostPaginationSpecification.Listing()
        sut.reset(specification)

        assertTrue(sut.canFetchMore)
        assertTrue(sut.history.isEmpty())
    }

    @Test
    fun givenNoResults_whenLoadNextPage_thenResultIsAsExpected() = runTest {
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
                auth = "fake-token",
                page = 1,
                pageCursor = null,
                limit = 20,
                type = specification.listingType,
                sort = specification.sortType,
                communityId = null,
                communityName = null,
                otherInstance = null,
            )
        }
    }

    @Test
    fun givenResults_whenLoadNextPage_thenResultIsAsExpected() = runTest {
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
                } to "page-cursor"
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
                auth = "fake-token",
                page = 1,
                pageCursor = null,
                limit = 20,
                type = specification.listingType,
                sort = specification.sortType,
                communityId = null,
                communityName = null,
                otherInstance = null,
            )
        }
    }

    @Test
    fun givenResults_whenSecondLoadNextPage_thenResultIsAsExpected() = runTest {
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
                } to "page-cursor"
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
                auth = "fake-token",
                page = 1,
                pageCursor = null,
                limit = 20,
                type = specification.listingType,
                sort = specification.sortType,
                communityId = null,
                communityName = null,
                otherInstance = null,
            )
            postRepository.getAll(
                auth = "fake-token",
                page = 2,
                pageCursor = "page-cursor",
                limit = 20,
                type = specification.listingType,
                sort = specification.sortType,
                communityId = null,
                communityName = null,
                otherInstance = null,
            )
        }
    }
}