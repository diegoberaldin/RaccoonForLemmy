package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetPostResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.GetPostsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.ListPostReportsResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.PostFeatureType
import com.github.diegoberaldin.raccoonforlemmy.core.api.dto.SuccessResponse
import com.github.diegoberaldin.raccoonforlemmy.core.api.provider.ServiceProvider
import com.github.diegoberaldin.raccoonforlemmy.core.api.service.PostService
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultPostRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val postService = mockk<PostService>()
    private val serviceProvider =
        mockk<ServiceProvider> {
            every { post } returns postService
        }
    private val customServiceProvider =
        mockk<ServiceProvider> {
            every { post } returns postService
        }

    private val sut =
        DefaultPostRepository(
            services = serviceProvider,
            customServices = customServiceProvider,
        )

    @Test
    fun givenNoResults_whenGetAll_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.getAll(
                    authHeader = any(),
                    auth = any(),
                    limit = any(),
                    sort = any(),
                    page = any(),
                    pageCursor = any(),
                    type = any(),
                    communityId = any(),
                    communityName = any(),
                )
            } returns
                GetPostsResponse(
                    posts = emptyList(),
                    nextPage = null,
                )

            val res =
                sut.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    type = ListingType.All,
                    sort = SortType.Active,
                )

            assertNotNull(res)
            val posts = res.first
            assertTrue(posts.isEmpty())
            assertNull(res.second)
            coVerify {
                postService.getAll(
                    auth = AUTH_TOKEN,
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    limit = 20,
                    sort = SortType.Active.toDto(),
                    page = 1,
                    pageCursor = null,
                    type = ListingType.All.toDto(),
                )
            }
        }

    @Test
    fun givenResults_whenGetAll_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.getAll(
                    authHeader = any(),
                    auth = any(),
                    limit = any(),
                    sort = any(),
                    page = any(),
                    pageCursor = any(),
                    type = any(),
                    communityId = any(),
                    communityName = any(),
                )
            } returns
                GetPostsResponse(
                    posts =
                        listOf(
                            mockk(relaxed = true) {
                                every { post } returns
                                    mockk(relaxed = true) { every { id } returns 1L }
                            },
                        ),
                    nextPage = PAGE_CURSOR,
                )

            val res =
                sut.getAll(
                    auth = AUTH_TOKEN,
                    page = 1,
                    pageCursor = null,
                    type = ListingType.All,
                    sort = SortType.Active,
                )

            assertNotNull(res)
            val posts = res.first
            assertTrue(posts.isNotEmpty())
            assertEquals(1L, posts.first().id)

            assertEquals(PAGE_CURSOR, res.second)
            coVerify {
                postService.getAll(
                    auth = AUTH_TOKEN,
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    limit = 20,
                    sort = SortType.Active.toDto(),
                    page = 1,
                    pageCursor = null,
                    type = ListingType.All.toDto(),
                )
            }
        }

    @Test
    fun givenResults_whenGet_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val postId = 1L
            coEvery {
                postService.get(
                    authHeader = any(),
                    auth = any(),
                    id = any(),
                    commentId = any(),
                )
            } returns
                GetPostResponse(
                    postView =
                        mockk(relaxed = true) {
                            every { post } returns
                                mockk(relaxed = true) { every { id } returns postId }
                        },
                    moderators = emptyList(),
                    communityView = mockk(relaxed = true),
                    crossPosts = emptyList(),
                )

            val res =
                sut.get(
                    id = postId,
                    auth = AUTH_TOKEN,
                )

            assertNotNull(res)
            assertEquals(postId, res.id)
        }

    @Test
    fun givenNonUpVotedPost_whenAsUpVoted_ThenResultIsAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = 0, upvotes = 1, score = 0, downvotes = 1)
            val res = sut.asUpVoted(post, true)

            assertEquals(1, res.myVote)
            assertEquals(2, res.upvotes)
            assertEquals(1, res.downvotes)
            assertEquals(1, res.score)
        }

    @Test
    fun givenDownVotedPost_whenAsUpVoted_ThenResultIsAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = -1, upvotes = 1, score = 0, downvotes = 1)
            val res = sut.asUpVoted(post, true)

            assertEquals(1, res.myVote)
            assertEquals(2, res.upvotes)
            assertEquals(0, res.downvotes)
            assertEquals(2, res.score)
        }

    @Test
    fun givenUpVotedPost_whenAsUpVotedUndo_ThenResultIsAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = 1, upvotes = 1, score = 0, downvotes = 1)
            val res = sut.asUpVoted(post, false)

            assertEquals(0, res.myVote)
            assertEquals(0, res.upvotes)
            assertEquals(1, res.downvotes)
            assertEquals(-1, res.score)
        }

    @Test
    fun whenUpVote_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = 1, upvotes = 1, score = 0, downvotes = 1)
            coEvery {
                postService.like(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val res =
                sut.upVote(
                    auth = AUTH_TOKEN,
                    post = post,
                    voted = true,
                )

            assertTrue(res.isSuccess)
            coVerify {
                postService.like(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(1L, it.postId)
                            assertEquals(1, it.score)
                            assertEquals(AUTH_TOKEN, it.auth)
                        },
                )
            }
        }

    @Test
    fun whenUpVoteUndo_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = 1, upvotes = 1, score = 0, downvotes = 1)
            coEvery {
                postService.like(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val res =
                sut.upVote(
                    auth = AUTH_TOKEN,
                    post = post,
                    voted = false,
                )

            assertTrue(res.isSuccess)
            coVerify {
                postService.like(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(1L, it.postId)
                            assertEquals(0, it.score)
                            assertEquals(AUTH_TOKEN, it.auth)
                        },
                )
            }
        }

    @Test
    fun givenNonDownVotedPost_whenAsDownVoted_ThenResultIsAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = 0, upvotes = 1, score = 0, downvotes = 1)
            val res = sut.asDownVoted(post, true)

            assertEquals(-1, res.myVote)
            assertEquals(1, res.upvotes)
            assertEquals(2, res.downvotes)
            assertEquals(-1, res.score)
        }

    @Test
    fun givenUpVotedPost_whenAsDownVoted_ThenResultIsAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = 1, upvotes = 1, score = 0, downvotes = 1)
            val res = sut.asDownVoted(post, true)

            assertEquals(-1, res.myVote)
            assertEquals(0, res.upvotes)
            assertEquals(2, res.downvotes)
            assertEquals(-2, res.score)
        }

    @Test
    fun givenDownVotedPost_whenAsDownVotedUndo_ThenResultIsAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = -1, upvotes = 1, score = 0, downvotes = 1)
            val res = sut.asDownVoted(post, false)

            assertEquals(0, res.myVote)
            assertEquals(1, res.upvotes)
            assertEquals(0, res.downvotes)
            assertEquals(1, res.score)
        }

    @Test
    fun whenDownVote_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = 1, upvotes = 1, score = 0, downvotes = 1)
            coEvery {
                postService.like(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val res =
                sut.downVote(
                    auth = AUTH_TOKEN,
                    post = post,
                    downVoted = true,
                )

            assertTrue(res.isSuccess)
            coVerify {
                postService.like(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(1L, it.postId)
                            assertEquals(-1, it.score)
                            assertEquals(AUTH_TOKEN, it.auth)
                        },
                )
            }
        }

    @Test
    fun whenDownVoteUndo_thenResultAndInteractionsAreAsExpected() =
        runTest {
            val post = PostModel(id = 1L, myVote = 1, upvotes = 1, score = 0, downvotes = 1)
            coEvery {
                postService.like(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val res =
                sut.downVote(
                    auth = AUTH_TOKEN,
                    post = post,
                    downVoted = false,
                )

            assertTrue(res.isSuccess)
            coVerify {
                postService.like(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(1L, it.postId)
                            assertEquals(0, it.score)
                            assertEquals(AUTH_TOKEN, it.auth)
                        },
                )
            }
        }

    @Test
    fun givenNonSavedPost_whenAsSaved_ThenResultIsAsExpected() =
        runTest {
            val post = PostModel(id = 1L, saved = false)
            val res = sut.asSaved(post, true)

            assertEquals(true, res.saved)
        }

    @Test
    fun givenSavedPost_whenAsSavedUndo_ThenResultIsAsExpected() =
        runTest {
            val post = PostModel(id = 1L, saved = true)
            val res = sut.asSaved(post, false)

            assertEquals(false, res.saved)
        }

    @Test
    fun whenSave_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.save(any(), any())
            } returns mockk(relaxed = true)

            val post = PostModel(id = 1L, saved = false)
            val res =
                sut.save(
                    post = post,
                    auth = AUTH_TOKEN,
                    saved = true,
                )

            assertTrue(res.isSuccess)
            coVerify {
                postService.save(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(1L, it.postId)
                            assertTrue(it.save)
                            assertEquals(AUTH_TOKEN, it.auth)
                        },
                )
            }
        }

    @Test
    fun whenSaveUndo_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.save(any(), any())
            } returns mockk(relaxed = true)

            val post = PostModel(id = 1L, saved = true)
            val res =
                sut.save(
                    post = post,
                    auth = AUTH_TOKEN,
                    saved = false,
                )

            assertTrue(res.isSuccess)
            coVerify {
                postService.save(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(1L, it.postId)
                            assertFalse(it.save)
                            assertEquals(AUTH_TOKEN, it.auth)
                        },
                )
            }
        }

    @Test
    fun whenCreate_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.create(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val communityId = 1L
            val title = "fake-title"
            val body = "fake-body"
            sut.create(
                communityId = communityId,
                title = title,
                body = body,
                auth = AUTH_TOKEN,
                nsfw = false,
                languageId = null,
            )

            coVerify {
                postService.create(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(communityId, it.communityId)
                            assertEquals(title, it.name)
                            assertEquals(body, it.body)
                            assertEquals(AUTH_TOKEN, it.auth)
                            assertEquals(false, it.nsfw)
                        },
                )
            }
        }

    @Test
    fun whenEdit_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.edit(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val postId = 1L
            val title = "fake-title-edit"
            val body = "fake-body-edit"
            sut.edit(
                postId = postId,
                title = title,
                body = body,
                auth = AUTH_TOKEN,
                nsfw = false,
                languageId = null,
            )

            coVerify {
                postService.edit(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                            assertEquals(title, it.name)
                            assertEquals(body, it.body)
                            assertEquals(AUTH_TOKEN, it.auth)
                            assertEquals(false, it.nsfw)
                        },
                )
            }
        }

    @Test
    fun whenSetRead_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.markAsRead(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val postId = 1L
            sut.setRead(
                postId = postId,
                read = true,
                auth = AUTH_TOKEN,
            )

            coVerify {
                postService.markAsRead(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                            assertEquals(AUTH_TOKEN, it.auth)
                            assertTrue(it.read)
                        },
                )
            }
        }

    @Test
    fun whenDelete_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.delete(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val postId = 1L
            sut.delete(
                id = postId,
                auth = AUTH_TOKEN,
            )

            coVerify {
                postService.delete(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                        },
                )
            }
        }

    @Test
    fun whenUploadImage_thenInteractionsAreAsExpected() =
        runTest {
            val instance = "fake-instance"
            every {
                serviceProvider.currentInstance
            } returns instance
            coEvery {
                postService.uploadImage(
                    authHeader = any(),
                    url = any(),
                    token = any(),
                    content = any(),
                )
            } returns mockk(relaxed = true)

            sut.uploadImage(
                auth = AUTH_TOKEN,
                bytes = byteArrayOf(),
            )

            coVerify {
                postService.uploadImage(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    url = "https://$instance/pictrs/image",
                    token = "jwt=$AUTH_TOKEN",
                    content = any(),
                )
            }
        }

    @Test
    fun whenReport_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.createReport(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val postId = 1L
            val reason = "fake-reason"
            sut.report(
                postId = postId,
                auth = AUTH_TOKEN,
                reason = reason,
            )

            coVerify {
                postService.createReport(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                            assertEquals(reason, it.reason)
                        },
                )
            }
        }

    @Test
    fun whenFeatureInCommunity_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.feature(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val postId = 1L
            sut.featureInCommunity(
                postId = postId,
                auth = AUTH_TOKEN,
                featured = true,
            )

            coVerify {
                postService.feature(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                            assertTrue(it.featured)
                            assertEquals(PostFeatureType.Community, it.featureType)
                        },
                )
            }
        }

    @Test
    fun whenFeatureInInstance_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.feature(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val postId = 1L
            sut.featureInInstance(
                postId = postId,
                auth = AUTH_TOKEN,
                featured = true,
            )

            coVerify {
                postService.feature(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                            assertTrue(it.featured)
                            assertEquals(PostFeatureType.Local, it.featureType)
                        },
                )
            }
        }

    @Test
    fun whenLock_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.lock(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val postId = 1L
            sut.lock(
                postId = postId,
                auth = AUTH_TOKEN,
                locked = true,
            )

            coVerify {
                postService.lock(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                            assertTrue(it.locked)
                        },
                )
            }
        }

    @Test
    fun whenRemove_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.remove(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val postId = 1L
            val reason = "fake-reason"
            sut.remove(
                postId = postId,
                auth = AUTH_TOKEN,
                removed = true,
                reason = reason,
            )

            coVerify {
                postService.remove(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                            assertTrue(it.removed)
                            assertEquals(reason, it.reason)
                        },
                )
            }
        }

    @Test
    fun whenGetReports_thenResultAndInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.listReports(
                    auth = any(),
                    authHeader = any(),
                    communityId = any(),
                    page = any(),
                    limit = any(),
                    unresolvedOnly = any(),
                )
            } returns ListPostReportsResponse(postReports = listOf(mockk(relaxed = true)))

            val communityId = 1L
            val res =
                sut.getReports(
                    auth = AUTH_TOKEN,
                    communityId = communityId,
                    page = 1,
                    limit = 20,
                    unresolvedOnly = true,
                )

            assertNotNull(res)
            assertEquals(1, res.size)
            coVerify {
                postService.listReports(
                    auth = AUTH_TOKEN,
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    communityId = communityId,
                    page = 1,
                    limit = 20,
                    unresolvedOnly = true,
                )
            }
        }

    @Test
    fun whenResolveReport_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.resolveReport(
                    authHeader = any(),
                    form = any(),
                )
            } returns mockk(relaxed = true)

            val reportId = 1L
            sut.resolveReport(
                reportId = reportId,
                auth = AUTH_TOKEN,
                resolved = true,
            )

            coVerify {
                postService.resolveReport(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(reportId, it.reportId)
                            assertTrue(it.resolved)
                        },
                )
            }
        }

    @Test
    fun whenPurge_thenInteractionsAreAsExpected() =
        runTest {
            coEvery {
                postService.purge(
                    authHeader = any(),
                    form = any(),
                )
            } returns SuccessResponse(true)

            val postId = 1L
            val reason = "fake-reason"
            sut.purge(
                postId = postId,
                auth = AUTH_TOKEN,
                reason = reason,
            )

            coVerify {
                postService.purge(
                    authHeader = AUTH_TOKEN.toAuthHeader(),
                    form =
                        withArg {
                            assertEquals(postId, it.postId)
                            assertEquals(reason, it.reason)
                        },
                )
            }
        }

    companion object {
        private const val AUTH_TOKEN = "fake-auth-token"
        private const val PAGE_CURSOR = "page-cursor"
    }
}
