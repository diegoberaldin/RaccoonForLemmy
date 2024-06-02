package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentSortType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListingType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SortType
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.CommentService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.PostService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.SearchService
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.UserService
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultUserRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val userService = mockk<UserService>()
    private val searchService = mockk<SearchService>()
    private val postService = mockk<PostService>()
    private val commentService = mockk<CommentService>()
    private val serviceProvider =
        mockk<ServiceProvider> {
            every { user } returns userService
            every { search } returns searchService
            every { post } returns postService
            every { comment } returns commentService
        }
    private val customServiceProvider =
        mockk<ServiceProvider> {
            every { user } returns userService
            every { search } returns searchService
            every { post } returns postService
            every { comment } returns commentService
        }

    private val sut = DefaultUserRepository(
        services = serviceProvider,
        customServices = customServiceProvider,
    )

    @Test
    fun whenGetResolved_thenResultAndInteractionsAreAsExpected() = runTest {
        val userId = 1L
        coEvery {
            searchService.resolveObject(any(), any())
        } returns mockk {
            every { user } returns mockk(relaxed = true) {
                every { person } returns mockk(relaxed = true) {
                    every { id } returns userId
                }
            }
        }

        val query = "!user@feddit.it"
        val res = sut.getResolved(query = query, auth = AUTH_TOKEN)

        assertNotNull(res)
        assertEquals(userId, res.id)
        coVerify {
            searchService.resolveObject(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                q = query,
            )
        }
    }

    @Test
    fun whenGet_thenResultAndInteractionsAreAsExpected() = runTest {
        val userId = 1L
        coEvery {
            userService.getDetails(
                authHeader = any(),
                auth = any(),
                personId = any(),
            )
        } returns mockk {
            every { personView } returns mockk(relaxed = true) {
                every { person } returns mockk(relaxed = true) {
                    every { id } returns userId
                }
            }
        }

        val res = sut.get(id = userId, auth = AUTH_TOKEN)

        assertNotNull(res)
        assertEquals(userId, res.id)
        coVerify {
            userService.getDetails(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                personId = userId,
            )
        }
    }

    @Test
    fun whenGetPosts_thenResultAndInteractionsAreAsExpected() = runTest {
        val postId = 1L
        coEvery {
            userService.getDetails(
                authHeader = any(),
                auth = any(),
                personId = any(),
                page = any(),
                limit = any(),
                sort = any(),
                username = any(),
                savedOnly = any(),
                communityId = any(),
            )
        } returns mockk {
            every { personView } returns mockk(relaxed = true) {
                every { posts } returns listOf(
                    mockk(relaxed = true) {
                        every { post } returns mockk(relaxed = true) {
                            every { id } returns postId
                        }
                    }
                )
            }
        }

        val res = sut.getPosts(id = postId, auth = AUTH_TOKEN, page = 1)

        assertNotNull(res)
        assertTrue(res.isNotEmpty())
        assertEquals(postId, res.first().id)
        coVerify {
            userService.getDetails(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                personId = postId,
                page = 1,
                limit = 20,
                sort = CommentSortType.New,
            )
        }
    }

    @Test
    fun whenGetSavedPosts_thenResultAndInteractionsAreAsExpected() = runTest {
        val postId = 1L
        coEvery {
            userService.getDetails(
                authHeader = any(),
                auth = any(),
                personId = any(),
                page = any(),
                limit = any(),
                sort = any(),
                username = any(),
                savedOnly = any(),
                communityId = any(),
            )
        } returns mockk {
            every { personView } returns mockk(relaxed = true) {
                every { posts } returns listOf(
                    mockk(relaxed = true) {
                        every { post } returns mockk(relaxed = true) {
                            every { id } returns postId
                        }
                    }
                )
            }
        }

        val res = sut.getSavedPosts(id = postId, auth = AUTH_TOKEN, page = 1)

        assertNotNull(res)
        assertTrue(res.isNotEmpty())
        assertEquals(postId, res.first().id)
        coVerify {
            userService.getDetails(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                personId = postId,
                savedOnly = true,
                page = 1,
                limit = 20,
                sort = CommentSortType.New,
            )
        }
    }

    @Test
    fun whenGetComments_thenResultAndInteractionsAreAsExpected() = runTest {
        val commentId = 1L
        coEvery {
            userService.getDetails(
                authHeader = any(),
                auth = any(),
                personId = any(),
                page = any(),
                limit = any(),
                sort = any(),
                username = any(),
                savedOnly = any(),
                communityId = any(),
            )
        } returns mockk {
            every { personView } returns mockk(relaxed = true) {
                every { comments } returns listOf(
                    mockk(relaxed = true) {
                        every { comment } returns mockk(relaxed = true) {
                            every { id } returns commentId
                        }
                    }
                )
            }
        }

        val res = sut.getComments(id = commentId, auth = AUTH_TOKEN, page = 1)

        assertNotNull(res)
        assertTrue(res.isNotEmpty())
        assertEquals(commentId, res.first().id)
        coVerify {
            userService.getDetails(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                personId = commentId,
                page = 1,
                limit = 20,
                sort = CommentSortType.New,
            )
        }
    }

    @Test
    fun whenGetSavedComments_thenResultAndInteractionsAreAsExpected() = runTest {
        val commentId = 1L
        coEvery {
            userService.getDetails(
                authHeader = any(),
                auth = any(),
                personId = any(),
                page = any(),
                limit = any(),
                sort = any(),
                username = any(),
                savedOnly = any(),
                communityId = any(),
            )
        } returns mockk {
            every { personView } returns mockk(relaxed = true) {
                every { comments } returns listOf(
                    mockk(relaxed = true) {
                        every { comment } returns mockk(relaxed = true) {
                            every { id } returns commentId
                        }
                    }
                )
            }
        }

        val res = sut.getSavedComments(id = commentId, auth = AUTH_TOKEN, page = 1)

        assertNotNull(res)
        assertTrue(res.isNotEmpty())
        assertEquals(commentId, res.first().id)
        coVerify {
            userService.getDetails(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                personId = commentId,
                savedOnly = true,
                page = 1,
                limit = 20,
                sort = CommentSortType.New,
            )
        }
    }

    @Test
    fun whenGetMentions_thenResultAndInteractionsAreAsExpected() = runTest {
        val mentionId = 1L
        coEvery {
            userService.getMentions(
                authHeader = any(),
                auth = any(),
                page = any(),
                limit = any(),
                sort = any(),
                unreadOnly = any(),
            )
        } returns mockk {
            every { mentions } returns listOf(
                mockk(relaxed = true) {
                    every { personMention } returns mockk(relaxed = true) {
                        every { id } returns mentionId
                    }

                }
            )
        }

        val res = sut.getMentions(auth = AUTH_TOKEN, page = 1, unreadOnly = true)

        assertNotNull(res)
        assertTrue(res.isNotEmpty())
        assertEquals(mentionId, res.first().id)
        coVerify {
            userService.getMentions(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                page = 1,
                limit = 20,
                sort = CommentSortType.New,
                unreadOnly = true,
            )
        }
    }

    @Test
    fun whenGetReplies_thenResultAndInteractionsAreAsExpected() = runTest {
        val replyId = 1L
        coEvery {
            userService.getReplies(
                authHeader = any(),
                auth = any(),
                page = any(),
                limit = any(),
                sort = any(),
                unreadOnly = any(),
            )
        } returns mockk {
            every { replies } returns listOf(
                mockk(relaxed = true) {
                    every { commentReply } returns mockk(relaxed = true) {
                        every { id } returns replyId
                    }
                }
            )
        }

        val res = sut.getReplies(auth = AUTH_TOKEN, page = 1, unreadOnly = true)

        assertNotNull(res)
        assertTrue(res.isNotEmpty())
        assertEquals(replyId, res.first().id)
        coVerify {
            userService.getReplies(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                page = 1,
                limit = 20,
                sort = CommentSortType.New,
                unreadOnly = true,
            )
        }
    }

    @Test
    fun whenSetMentionRead_thenInteractionsAreAsExpected() = runTest {
        coEvery {
            userService.markPersonMentionAsRead(any(), any())
        } returns mockk(relaxed = true)

        val mentionId = 1L
        sut.setMentionRead(
            read = true,
            mentionId = mentionId,
            auth = AUTH_TOKEN,

            )

        coVerify {
            userService.markPersonMentionAsRead(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                form = withArg {
                    assertEquals(mentionId, it.mentionId)
                    assertEquals(true, it.read)
                    assertEquals(AUTH_TOKEN, it.auth)
                }
            )
        }
    }

    @Test
    fun whenSetReplyRead_thenInteractionsAreAsExpected() = runTest {
        coEvery {
            commentService.markAsRead(any(), any())
        } returns mockk(relaxed = true)

        val mentionId = 1L
        sut.setReplyRead(
            read = true,
            replyId = mentionId,
            auth = AUTH_TOKEN,

            )

        coVerify {
            commentService.markAsRead(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                form = withArg {
                    assertEquals(mentionId, it.replyId)
                    assertEquals(true, it.read)
                    assertEquals(AUTH_TOKEN, it.auth)
                }
            )
        }
    }

    @Test
    fun whenBlock_thenInteractionsAreAsExpected() = runTest {
        coEvery {
            userService.block(any(), any())
        } returns mockk(relaxed = true)

        val userId = 1L
        sut.block(
            id = userId,
            auth = AUTH_TOKEN,
            blocked = true,
        )

        coVerify {
            userService.block(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                form = withArg {
                    assertEquals(userId, it.personId)
                    assertEquals(true, it.block)
                    assertEquals(AUTH_TOKEN, it.auth)
                }
            )
        }
    }

    @Test
    fun whenGetModeratedCommunities_thenResultAndInteractionsAreAsExpected() = runTest {
        val userId = 1L
        val communityId = 2L
        coEvery {
            userService.getDetails(
                authHeader = any(),
                auth = any(),
                personId = any(),
                page = any(),
                limit = any(),
                sort = any(),
                username = any(),
                savedOnly = any(),
                communityId = any(),
            )
        } returns mockk {
            every { personView } returns mockk(relaxed = true) {
                every { moderates } returns listOf(
                    mockk(relaxed = true) {
                        every { community } returns mockk(relaxed = true) {
                            every { id } returns communityId
                        }
                    }
                )
            }
        }

        val res = sut.getModeratedCommunities(auth = AUTH_TOKEN, id = userId)

        assertNotNull(res)
        assertTrue(res.isNotEmpty())
        assertEquals(communityId, res.first().id)
        coVerify {
            userService.getDetails(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                personId = userId,
            )
        }
    }

    @Test
    fun whenGetLikedPosts_thenResultAndInteractionsAreAsExpected() = runTest {
        val postId = 1L
        coEvery {
            postService.getAll(
                authHeader = any(),
                auth = any(),
                page = any(),
                limit = any(),
                pageCursor = any(),
                sort = any(),
                type = any(),
                likedOnly = any(),
                dislikedOnly = any(),
            )
        } returns mockk {
            every { posts } returns listOf(
                mockk(relaxed = true) {
                    every { post } returns mockk(relaxed = true) { every { id } returns postId }
                }
            )
            every { nextPage } returns PAGE_CURSOR
        }

        val res = sut.getLikedPosts(
            auth = AUTH_TOKEN,
            page = 1,
            liked = true,
        )

        assertNotNull(res)
        val posts = res.first
        assertTrue(posts.isNotEmpty())
        assertEquals(postId, posts.first().id)
        assertEquals(PAGE_CURSOR, res.second)
        coVerify {
            postService.getAll(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                page = 1,
                limit = 20,
                type = ListingType.All,
                pageCursor = null,
                sort = SortType.New,
                likedOnly = true,
                dislikedOnly = null,
            )
        }
    }

    @Test
    fun whenGetLikedComments_thenResultAndInteractionsAreAsExpected() = runTest {
        val commentId = 1L
        coEvery {
            commentService.getAll(
                authHeader = any(),
                auth = any(),
                page = any(),
                limit = any(),
                sort = any(),
                type = any(),
                likedOnly = any(),
                dislikedOnly = any(),
            )
        } returns mockk {
            every { comments } returns listOf(
                mockk(relaxed = true) {
                    every { comment } returns mockk(relaxed = true) { every { id } returns commentId }
                }
            )
        }

        val res = sut.getLikedComments(
            auth = AUTH_TOKEN,
            page = 1,
            liked = true,
        )

        assertNotNull(res)
        assertTrue(res.isNotEmpty())
        assertEquals(commentId, res.first().id)
        coVerify {
            commentService.getAll(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                auth = AUTH_TOKEN,
                page = 1,
                limit = 20,
                type = ListingType.All,
                sort = CommentSortType.New,
                likedOnly = true,
                dislikedOnly = null,
            )
        }
    }

    @Test
    fun whenPurge_thenInteractionsAreAsExpected() = runTest {
        coEvery {
            userService.purge(any(), any())
        } returns mockk {
            every { success } returns true
        }

        val userId = 1L
        val reason = "fake-reason"
        sut.purge(
            id = userId,
            auth = AUTH_TOKEN,
            reason = reason,
        )

        coVerify {
            userService.purge(
                authHeader = AUTH_TOKEN.toAuthHeader(),
                form = withArg {
                    assertEquals(userId, it.personId)
                    assertEquals(reason, it.reason)
                }
            )
        }
    }

    companion object {
        private const val AUTH_TOKEN = "fake-auth-token"
        private const val PAGE_CURSOR = "fake-page-cursor"
    }
}