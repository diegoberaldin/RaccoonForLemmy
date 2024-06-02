package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination

import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
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

class DefaultCommentPaginationManagerTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val identityRepository: IdentityRepository =
        mockk {
            every { authToken } returns MutableStateFlow(AUTH_TOKEN)
        }
    private val commentRepository: CommentRepository = mockk(relaxUnitFun = true)
    private val userRepository: UserRepository = mockk(relaxUnitFun = true)
    private val notificationCenter: NotificationCenter = mockk(relaxUnitFun = true) {
        val slot = slot<KClass<NotificationCenterEvent>>()
        every { subscribe(capture(slot)) } answers { MutableSharedFlow() }
    }

    private val sut =
        DefaultCommentPaginationManager(
            identityRepository = identityRepository,
            commentRepository = commentRepository,
            userRepository = userRepository,
            notificationCenter = notificationCenter,
            dispatcher = dispatcherTestRule.dispatcher,
        )

    @Test
    fun whenReset_thenHistoryIsClearedAndCanFetchMore() =
        runTest {
            val specification = CommentPaginationSpecification.Replies()
            sut.reset(specification)

            assertTrue(sut.canFetchMore)
        }

    @Test
    fun givenRepliesSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                commentRepository.getAll(
                    auth = any(),
                    page = any(),
                    postId = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    maxDepth = any(),
                )
            } returns emptyList()
            val postId = 1L
            val specification = CommentPaginationSpecification.Replies(postId = postId)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                commentRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    postId = postId,
                    limit = 20,
                    type = specification.listingType ?: ListingType.All,
                    sort = specification.sortType,
                    maxDepth = 6,
                )
            }
        }

    @Test
    fun givenRepliesSpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val postId = 1L
            val page = slot<Int>()
            coEvery {
                commentRepository.getAll(
                    auth = any(),
                    page = capture(page),
                    postId = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    maxDepth = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        CommentModel(id = idx.toLong(), text = "", postId = postId)
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommentPaginationSpecification.Replies(postId = postId)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                commentRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    postId = postId,
                    limit = 20,
                    type = specification.listingType ?: ListingType.All,
                    sort = specification.sortType,
                    maxDepth = 6,
                )
            }
        }

    @Test
    fun givenRepliesSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            val postId = 1L
            coEvery {
                commentRepository.getAll(
                    auth = any(),
                    page = capture(page),
                    postId = any(),
                    limit = any(),
                    type = any(),
                    sort = any(),
                    maxDepth = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        CommentModel(id = idx.toLong(), text = "", postId = postId)
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommentPaginationSpecification.Replies(postId = postId)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                commentRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    postId = postId,
                    limit = 20,
                    type = specification.listingType ?: ListingType.All,
                    sort = specification.sortType,
                    maxDepth = 6,
                )
                commentRepository.getAll(
                    auth = AUTH_TOKEN,
                    page = 2,
                    postId = postId,
                    limit = 20,
                    type = specification.listingType ?: ListingType.All,
                    sort = specification.sortType,
                    maxDepth = 6,
                )
            }
        }

    @Test
    fun givenUsersSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                userRepository.getComments(
                    auth = any(),
                    page = any(),
                    id = any(),
                    limit = any(),
                    sort = any(),
                    username = any(),
                    otherInstance = any(),
                )
            } returns emptyList()
            val userId = 1L
            val specification = CommentPaginationSpecification.User(id = userId)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                userRepository.getComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    id = userId,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenUserSpecAndResultswhenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val userId = 1L
            val page = slot<Int>()
            coEvery {
                userRepository.getComments(
                    auth = any(),
                    page = capture(page),
                    id = any(),
                    limit = any(),
                    sort = any(),
                    username = any(),
                    otherInstance = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        CommentModel(id = idx.toLong(), text = "")
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommentPaginationSpecification.User(id = userId)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                userRepository.getComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    id = userId,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenUserSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            val userId = 1L
            coEvery {
                userRepository.getComments(
                    auth = any(),
                    page = capture(page),
                    id = any(),
                    limit = any(),
                    sort = any(),
                    username = any(),
                    otherInstance = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        CommentModel(id = idx.toLong(), text = "")
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommentPaginationSpecification.User(id = userId)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                userRepository.getComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    id = userId,
                    limit = 20,
                    sort = specification.sortType,
                )
                userRepository.getComments(
                    auth = AUTH_TOKEN,
                    page = 2,
                    id = userId,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenVotesSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            coEvery {
                userRepository.getLikedComments(
                    auth = any(),
                    page = any(),
                    liked = any(),
                    limit = any(),
                    sort = any(),
                )
            } returns emptyList()
            val specification = CommentPaginationSpecification.Votes(liked = true)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                userRepository.getLikedComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    liked = specification.liked,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenVotesSpecAndResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                userRepository.getLikedComments(
                    auth = any(),
                    page = capture(page),
                    liked = any(),
                    limit = any(),
                    sort = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        CommentModel(id = idx.toLong(), text = "")
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommentPaginationSpecification.Votes(liked = true)
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                userRepository.getLikedComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    liked = specification.liked,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenVotesSpecAndResults_whenSecondLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val page = slot<Int>()
            coEvery {
                userRepository.getLikedComments(
                    auth = any(),
                    page = capture(page),
                    liked = any(),
                    limit = any(),
                    sort = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        CommentModel(id = idx.toLong(), text = "")
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommentPaginationSpecification.Votes(liked = true)
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                userRepository.getLikedComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    liked = specification.liked,
                    limit = 20,
                    sort = specification.sortType,
                )
                userRepository.getLikedComments(
                    auth = AUTH_TOKEN,
                    page = 2,
                    liked = specification.liked,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    @Test
    fun givenSavedSpecAndNoResults_whenLoadNextPage_thenResultIsAsExpected() =
        runTest {
            val userId = 1L
            coEvery { identityRepository.cachedUser } returns UserModel(id = userId)
            coEvery {
                userRepository.getSavedComments(
                    auth = any(),
                    page = any(),
                    id = any(),
                    limit = any(),
                    sort = any(),
                )
            } returns emptyList()
            val specification = CommentPaginationSpecification.Saved()
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertTrue(items.isEmpty())
            coVerify {
                userRepository.getSavedComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    id = userId,
                    limit = 20,
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
                userRepository.getSavedComments(
                    auth = any(),
                    page = capture(page),
                    id = any(),
                    limit = any(),
                    sort = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        CommentModel(id = idx.toLong(), text = "")
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommentPaginationSpecification.Saved()
            sut.reset(specification)

            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertTrue(sut.canFetchMore)

            coVerify {
                userRepository.getSavedComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    id = userId,
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
                userRepository.getSavedComments(
                    auth = any(),
                    page = capture(page),
                    id = any(),
                    limit = any(),
                    sort = any(),
                )
            } answers {
                val pageNumber = page.captured
                if (pageNumber == 1) {
                    (0..<20).map { idx ->
                        CommentModel(id = idx.toLong(), text = "")
                    }
                } else {
                    emptyList()
                }
            }
            val specification = CommentPaginationSpecification.Saved()
            sut.reset(specification)

            sut.loadNextPage()
            val items = sut.loadNextPage()

            assertEquals(20, items.size)
            assertFalse(sut.canFetchMore)

            coVerifySequence {
                userRepository.getSavedComments(
                    auth = AUTH_TOKEN,
                    page = 1,
                    id = userId,
                    limit = 20,
                    sort = specification.sortType,
                )
                userRepository.getSavedComments(
                    auth = AUTH_TOKEN,
                    page = 2,
                    id = userId,
                    limit = 20,
                    sort = specification.sortType,
                )
            }
        }

    companion object {
        private const val AUTH_TOKEN = "fake-token"
    }
}
