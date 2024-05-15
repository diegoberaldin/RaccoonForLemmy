package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import app.cash.sqldelight.Query
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.MultiCommunityEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.MulticommunitiesQueries
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.entities.AppDatabase
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultMultiCommunityRepositoryTest {

    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val query = mockk<Query<MultiCommunityEntity>>()
    private val queries = mockk<MulticommunitiesQueries>(relaxUnitFun = true) {
        every { getAll(any()) } returns query
        every { getBy(name = any(), account_id = any()) } returns query
        every { getById(any()) } returns query
    }
    private val provider = mockk<DatabaseProvider> {
        every { getDatabase() } returns mockk<AppDatabase> {
            every { multicommunitiesQueries } returns queries
        }
    }

    private val sut = DefaultMultiCommunityRepository(provider)

    @Test
    fun givenEmpty_whenGetAll_thenResultIsAsExpected() = runTest {
        every { query.executeAsList() } returns listOf()

        val res = sut.getAll(1)

        assertTrue(res.isEmpty())
        verify {
            queries.getAll(1)
        }
    }

    @Test
    fun givenNotEmpty_whenGetAll_thenResultIsAsExpected() = runTest {
        every { query.executeAsList() } returns listOf(createFakeMultiCommunityEntity(id = 2))

        val res = sut.getAll(1)

        assertTrue(res.isNotEmpty())
        assertEquals(2, res.first().id)
        verify {
            queries.getAll(1)
        }
    }

    @Test
    fun givenEmpty_whenGetById_thenResultIsAsExpected() = runTest {
        every { query.executeAsOneOrNull() } returns null

        val res = sut.getById(1)

        assertNull(res)
        verify {
            queries.getById(1)
        }
    }

    @Test
    fun givenNotEmpty_whenGetById_thenResultIsAsExpected() = runTest {
        every { query.executeAsOneOrNull() } returns createFakeMultiCommunityEntity(id = 2)

        val res = sut.getById(2)

        assertNotNull(res)
        assertEquals(2, res.id)
        verify {
            queries.getById(2)
        }
    }

    @Test
    fun whenCreate_thenInteractionsAreAsExpected() = runTest {
        val model = MultiCommunityModel(name = "test", communityIds = listOf(1, 2, 3))
        every { query.executeAsOneOrNull() } returns createFakeMultiCommunityEntity(id = 2)

        val res = sut.create(model = model, accountId = 1)

        assertEquals(2, res)
        verify {
            queries.create(
                name = "test",
                icon = null,
                communityIds = "1,2,3",
                account_id = 1,
            )
        }
    }

    @Test
    fun whenUpdate_thenInteractionsAreAsExpected() = runTest {
        val model = MultiCommunityModel(name = "test", id = 2, icon = "fake-icon", communityIds = listOf(1, 2, 3))
        every { query.executeAsOneOrNull() } returns createFakeMultiCommunityEntity(id = 2)

        sut.update(model = model)

        verify {
            queries.update(
                id = 2,
                name = "test",
                icon = "fake-icon",
                communityIds = "1,2,3",
            )
        }
    }

    @Test
    fun whenDelete_thenInteractionsAreAsExpected() = runTest {
        val model = MultiCommunityModel(name = "test", id = 2)
        every { query.executeAsOneOrNull() } returns createFakeMultiCommunityEntity(id = 2)

        sut.delete(model)

        verify {
            queries.delete(id = 2)
        }
    }

    private fun createFakeMultiCommunityEntity(
        id: Long,
        name: String = "",
        icon: String? = null,
        communityIds: String = "",
        accountId: Long? = null,
    ) = MultiCommunityEntity(
        id = id,
        name = name,
        icon = icon,
        communityIds = communityIds,
        account_id = accountId,
    )
}
