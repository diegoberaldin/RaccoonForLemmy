package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import app.cash.sqldelight.Query
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DraftEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DraftsQueries
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftType
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.entities.AppDatabase
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultDraftRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val query = mockk<Query<DraftEntity>>()
    private val queries =
        mockk<DraftsQueries>(relaxUnitFun = true) {
            every { getBy(any()) } returns query
            every { getAllBy(type = any(), account_id = any()) } returns query
        }
    private val provider =
        mockk<DatabaseProvider> {
            every { getDatabase() } returns
                mockk<AppDatabase> {
                    every { draftsQueries } returns queries
                }
        }

    private val sut = DefaultDraftRepository(provider)

    @Test
    fun givenNotExisting_whenGetBy_thenResultIsAsExpected() =
        runTest {
            every { query.executeAsOneOrNull() } returns null

            val res = sut.getBy(1)

            assertNull(res)
            verify {
                queries.getBy(1)
            }
        }

    @Test
    fun givenExisting_whenGetBy_thenResultIsAsExpected() =
        runTest {
            every { query.executeAsOneOrNull() } returns createFakeDraftEntity(1)

            val res = sut.getBy(1)

            assertNotNull(res)
            assertEquals(1, res.id)
            verify {
                queries.getBy(1)
            }
        }

    @Test
    fun givenEmpty_whenGetAllByPosts_thenResultIsAsExpected() =
        runTest {
            every { query.executeAsList() } returns emptyList()

            val res = sut.getAll(type = DraftType.Post, accountId = 1)

            assertTrue(res.isEmpty())
            verify {
                queries.getAllBy(0, 1)
            }
        }

    @Test
    fun givenEmpty_whenGetAllByComments_thenResultIsAsExpected() =
        runTest {
            every { query.executeAsList() } returns emptyList()

            val res = sut.getAll(type = DraftType.Comment, accountId = 1)

            assertTrue(res.isEmpty())
            verify {
                queries.getAllBy(1, 1)
            }
        }

    @Test
    fun givenNotEmpty_whenGetAllByPosts_thenResultIsAsExpected() =
        runTest {
            every { query.executeAsList() } returns listOf(createFakeDraftEntity(id = 2))

            val res = sut.getAll(type = DraftType.Post, accountId = 1)

            assertTrue(res.isNotEmpty())
            assertEquals(2, res.first().id)
            verify {
                queries.getAllBy(0, 1)
            }
        }

    @Test
    fun whenCreate_thenInteractionsAreAsExpected() =
        runTest {
            val text = "text"
            val model =
                DraftModel(
                    body = text,
                    type = DraftType.Post,
                    communityId = 3,
                )

            sut.create(model, 1)

            verify {
                queries.create(
                    type = 0,
                    body = text,
                    title = null,
                    url = null,
                    postId = null,
                    parentId = null,
                    communityId = 3,
                    languageId = null,
                    nsfw = null,
                    date = null,
                    info = null,
                    account_id = 1,
                )
            }
        }

    @Test
    fun whenUpdate_thenInteractionsAreAsExpected() =
        runTest {
            val text = "text"
            val model =
                DraftModel(
                    id = 1,
                    body = text,
                    type = DraftType.Post,
                    communityId = 3,
                )

            sut.update(model)

            verify {
                queries.update(
                    id = 1,
                    body = text,
                    title = null,
                    url = null,
                    communityId = 3,
                    languageId = null,
                    nsfw = null,
                    date = null,
                    info = null,
                )
            }
        }

    @Test
    fun whenDelete_thenInteractionsAreAsExpected() =
        runTest {
            sut.delete(1)

            verify {
                queries.delete(1)
            }
        }

    private fun createFakeDraftEntity(
        id: Long = 0,
        accountId: Long = 0,
        type: DraftType = DraftType.Post,
        title: String? = null,
        body: String = "",
        postId: Long? = null,
        parentId: Long? = null,
        communityId: Long? = null,
        languageId: Long? = null,
        url: String? = null,
        nsfw: Boolean = false,
        info: String? = null,
        date: Long? = null,
    ) = DraftEntity(
        id = id,
        title = title,
        body = body,
        type = if (type == DraftType.Post) 0 else 1,
        postId = postId,
        parentId = parentId,
        account_id = accountId,
        communityId = communityId,
        languageId = languageId,
        url = url,
        nsfw = if (nsfw) 1 else 0,
        info = info,
        date = date,
    )
}
