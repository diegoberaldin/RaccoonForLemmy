package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DefaultMultiCommunityPaginatorTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val postRepository: PostRepository = mockk()

    private val sut =
        DefaultMultiCommunityPaginator(
            postRepository = postRepository,
        )

    @Test
    fun whenLoadNextPage_thenResultAndInteractionsAreAsExpected() =
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
            val communityIds = listOf(1L, 2L)
            sut.setCommunities(communityIds)

            val sort = SortType.Active
            val items =
                sut.loadNextPage(
                    auth = AUTH_TOKEN,
                    sort = sort,
                )

            assertEquals(40, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    type = ListingType.All,
                    sort = sort,
                    communityId = 1L,
                    communityName = null,
                )
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    type = ListingType.All,
                    sort = sort,
                    communityId = 2L,
                    communityName = null,
                )
            }
        }

    @Test
    fun whenLoadNextPageAndNoMoreResults_thenResultAndInteractionsAreAsExpected() =
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
            val communityIds = listOf(1L, 2L)
            sut.setCommunities(communityIds)

            val sort = SortType.Active
            sut.loadNextPage(
                auth = AUTH_TOKEN,
                sort = sort,
            )
            val items =
                sut.loadNextPage(
                    auth = AUTH_TOKEN,
                    sort = sort,
                )

            assertTrue(items.isEmpty())
            assertFalse(sut.canFetchMore)

            coVerify {
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    type = ListingType.All,
                    sort = sort,
                    communityId = 1L,
                    communityName = null,
                )
                postRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    limit = 20,
                    type = ListingType.All,
                    sort = sort,
                    communityId = 2L,
                    communityName = null,
                )
            }
        }

    companion object {
        private const val AUTH_TOKEN = "fake-token"
        private const val PAGE_CURSOR = "fake-page-cursor"
    }
}
