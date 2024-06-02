package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultPostNavigationManagerTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val postPaginationManager: PostPaginationManager = mockk(relaxUnitFun = true)

    private val sut =
        DefaultPostNavigationManager(
            postPaginationManager = postPaginationManager,
        )

    @Test
    fun whenInitial_thenCanNotNavigate() =
        runTest {
            val res = sut.canNavigate.value
            assertFalse(res)
        }

    @Test
    fun whenPush_thenCanNavigate() =
        runTest {
            val mockState = mockk<PostPaginationManagerState>()
            sut.push(mockState)

            val res = sut.canNavigate.value
            assertTrue(res)

            verify {
                postPaginationManager.restoreState(mockState)
            }
        }

    @Test
    fun givenEmpty_whenPop_thenCanNotNavigate() {
        sut.pop()

        val res = sut.canNavigate.value

        assertFalse(res)
    }

    @Test
    fun whenPopWithMoreThanOneState_thenCanNavigate() =
        runTest {
            val mockState1 = mockk<PostPaginationManagerState>()
            sut.push(mockState1)
            val mockState2 = mockk<PostPaginationManagerState>()
            sut.push(mockState2)

            sut.pop()
            val res = sut.canNavigate.value
            assertTrue(res)

            verifySequence {
                postPaginationManager.restoreState(mockState1)
                postPaginationManager.restoreState(mockState2)
                postPaginationManager.restoreState(mockState1)
            }
        }

    @Test
    fun whenPopWithOneState_thenCanNotNavigate() =
        runTest {
            val mockState = mockk<PostPaginationManagerState>()
            sut.push(mockState)

            sut.pop()
            val res = sut.canNavigate.value
            assertFalse(res)

            verifySequence {
                postPaginationManager.restoreState(mockState)
                postPaginationManager.reset()
            }
        }

    @Test
    fun givenEmptyHistory_whenGetPrevious_thenResultIsAsExpected() =
        runTest {
            every { postPaginationManager.history } returns emptyList()

            val res = sut.getPrevious(1)

            assertNull(res)
        }

    @Test
    fun givenHistory_whenGetPreviousWithFirstId_thenResultIsAsExpected() =
        runTest {
            val postId = 1L
            every { postPaginationManager.history } returns listOf(PostModel(id = postId))

            val res = sut.getPrevious(postId)

            assertNull(res)
        }

    @Test
    fun givenHistory_whenGetPreviousWithNotFirstId_thenResultIsAsExpected() =
        runTest {
            val otherPostId = 1L
            val postId = 2L
            every { postPaginationManager.history } returns
                    listOf(
                        PostModel(id = otherPostId),
                        PostModel(id = postId),
                    )

            val res = sut.getPrevious(postId)

            assertNotNull(res)
            assertEquals(otherPostId, res.id)
        }

    @Test
    fun givenEmptyHistory_whenGetNext_thenResultIsAsExpected() =
        runTest {
            every { postPaginationManager.history } returns emptyList()

            val res = sut.getNext(1)

            assertNull(res)
        }

    @Test
    fun givenHistoryAndCanNotFetchMore_whenGetNextWithLastId_thenResultIsAsExpected() =
        runTest {
            val postId = 1L
            every { postPaginationManager.history } returns listOf(PostModel(id = postId))
            every { postPaginationManager.canFetchMore } returns false

            val res = sut.getNext(postId)

            assertNull(res)
            coVerify(inverse = true) {
                postPaginationManager.loadNextPage()
            }
        }

    @Test
    fun givenHistoryAndCanFetchMore_whenGetNextWithLastId_thenResultIsAsExpected() =
        runTest {
            val postId = 1L
            val otherPostId = 2L
            every { postPaginationManager.history } returns listOf(PostModel(id = postId))
            every { postPaginationManager.canFetchMore } returns true
            coEvery { postPaginationManager.loadNextPage() } returns listOf(PostModel(id = otherPostId))

            val res = sut.getNext(postId)

            assertNotNull(res)
            assertEquals(otherPostId, res.id)

            coVerify {
                postPaginationManager.loadNextPage()
            }
        }

    @Test
    fun givenHistory_whenGetPreviousWithNotLastId_thenResultIsAsExpected() =
        runTest {
            val otherPostId = 1L
            val postId = 2L
            every { postPaginationManager.history } returns
                    listOf(
                        PostModel(id = postId),
                        PostModel(id = otherPostId),
                    )

            val res = sut.getNext(postId)

            assertNotNull(res)
            assertEquals(otherPostId, res.id)
        }
}
