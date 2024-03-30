package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CommentResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.CreateCommentLikeForm
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetCommentResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetCommentsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.CommentService
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toAuthHeader
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toCommentDto
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.utils.toDto
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DefaultCommentRepositoryTest {

    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val commentService = mockk<CommentService>()
    private val serviceProvider = mockk<ServiceProvider> {
        every { comment } returns commentService
    }
    private val customServiceProvider = mockk<ServiceProvider> {
        every { comment } returns commentService
    }
    private val sut = DefaultCommentRepository(
        services = serviceProvider,
        customServices = customServiceProvider,
    )

    @Test
    fun givenSuccess_whenGetAll_thenResultIsAsExpected() = runTest {
        coEvery {
            commentService.getAll(
                authHeader = any(),
                auth = any(),
                limit = any(),
                sort = any(),
                postId = any(),
                parentId = any(),
                page = any(),
                maxDepth = any(),
                type = any(),
                communityId = any(),
                communityName = any(),
                savedOnly = any(),
                likedOnly = any(),
                dislikedOnly = any(),
            )
        } returns mockk {
            every { isSuccessful } returns true
            every { body() } returns GetCommentsResponse(comments = listOf(mockk(relaxed = true)))
        }
        val token = "fake-token"
        val res = sut.getAll(
            postId = 1,
            auth = token,
            page = 1,
            type = ListingType.All,
            sort = SortType.New,
            limit = 20,
            maxDepth = 6,
        )

        assertNotNull(res)
        assertEquals(1, res.size)

        coVerify {
            customServiceProvider wasNot Called
            commentService.getAll(
                authHeader = token.toAuthHeader(),
                auth = token,
                postId = 1,
                page = 1,
                type = ListingType.All.toDto(),
                sort = SortType.New.toCommentDto(),
                limit = 20,
                maxDepth = 6,
            )
        }
    }

    @Test
    fun givenSuccess_whenGetBy_thenResultIsAsExpected() = runTest {
        val token = "fake-token"
        val commentId = 1
        coEvery { commentService.getBy(any(), any(), any()) } returns mockk {
            every { isSuccessful } returns true
            every { body() } returns GetCommentResponse(
                commentView = mockk(relaxed = true) {
                    every { comment } returns mockk(relaxed = true) { every { id } returns commentId }
                },
            )
        }

        val res = sut.getBy(id = commentId, auth = token)

        assertNotNull(res)
        assertEquals(1, res.id)
        coVerify {
            customServiceProvider wasNot Called
            commentService.getBy(
                authHeader = token.toAuthHeader(),
                id = commentId,
                auth = token,
            )
        }
    }

    @Test
    fun whenGetChildren_thenResultIsAsExpected() = runTest {
        coEvery {
            commentService.getAll(
                authHeader = any(),
                auth = any(),
                limit = any(),
                sort = any(),
                postId = any(),
                parentId = any(),
                page = any(),
                maxDepth = any(),
                type = any(),
                communityId = any(),
                communityName = any(),
                savedOnly = any(),
                likedOnly = any(),
                dislikedOnly = any(),
            )
        } returns mockk {
            every { isSuccessful } returns true
            every { body() } returns GetCommentsResponse(comments = listOf(mockk(relaxed = true)))
        }
        val token = "fake-token"
        val res = sut.getChildren(
            parentId = 2,
            auth = token,
            type = ListingType.All,
            sort = SortType.New,
            limit = 20,
            maxDepth = 6,
        )

        assertNotNull(res)
        assertEquals(1, res.size)

        coVerify {
            customServiceProvider wasNot Called
            commentService.getAll(
                authHeader = token.toAuthHeader(),
                auth = token,
                parentId = 2,
                type = ListingType.All.toDto(),
                sort = SortType.New.toCommentDto(),
                limit = 20,
                maxDepth = 6,
            )
        }
    }

    @Test
    fun whenAsUpVoted_thenResultIsAsExpected() = runTest {
        val comment = CommentModel(text = "text")

        val res = sut.asUpVoted(comment, true)

        assertEquals(1, res.myVote)
        assertEquals(1, res.score)
        assertEquals(1, res.upvotes)

        val afterRes = sut.asUpVoted(res, false)
        assertEquals(0, afterRes.myVote)
        assertEquals(0, afterRes.score)
        assertEquals(0, afterRes.upvotes)
    }

    @Test
    fun givenSuccess_whenUpVote_thenResultIsAsExpected() = runTest {
        val comment = CommentModel(id = 1, text = "text")
        val token = "fake-token"
        coEvery {
            commentService.like(
                any(), any()
            )
        } returns mockk {
            every { isSuccessful } returns true
            every { body() } returns CommentResponse(
                commentView = mockk(relaxed = true),
                recipientIds = listOf(),
            )
        }

        val res = sut.upVote(comment, token, true)

        assertNotNull(res)
        coVerify {
            commentService.like(
                authHeader = token.toAuthHeader(),
                form = CreateCommentLikeForm(
                    commentId = 1,
                    score = 1,
                    auth = token,
                )
            )
        }
    }
}
