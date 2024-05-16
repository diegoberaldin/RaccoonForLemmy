package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import app.cash.sqldelight.Query
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.AccountEntity
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.AccountsQueries
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.DatabaseProvider
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.AccountModel
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

class DefaultAccountRepositoryTest {
    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    private val query = mockk<Query<AccountEntity>>()
    private val queries =
        mockk<AccountsQueries>(relaxUnitFun = true) {
            every { getAll() } returns query
            every { getActive() } returns query
            every { getBy(username = any(), instance = any()) } returns query
            every { getActive() } returns query
        }
    private val provider =
        mockk<DatabaseProvider> {
            every { getDatabase() } returns
                mockk<AppDatabase> {
                    every { accountsQueries } returns queries
                }
        }

    private val sut =
        DefaultAccountRepository(
            provider = provider,
        )

    @Test
    fun givenNoAccounts_whenGetAll_thenResultIsAsExpected() =
        runTest {
            every { query.executeAsList() } returns listOf()

            val res = sut.getAll()

            assertTrue(res.isEmpty())
            verify {
                queries.getAll()
            }
        }

    @Test
    fun givenExitingAccounts_whenGetAll_thenResultIsAsExpected() =
        runTest {
            val accounts = listOf(createFakeEntity())
            every { query.executeAsList() } returns accounts

            val res = sut.getAll()

            assertTrue(res.size == 1)
            verify {
                queries.getAll()
            }
        }

    @Test
    fun givenNoActiveAccount_whenGetActive_thenResultIsAsExpected() =
        runTest {
            every { query.executeAsOneOrNull() } returns null

            val res = sut.getActive()

            assertNull(res)
            verify {
                queries.getActive()
            }
        }

    @Test
    fun givenActiveAccount_whenGetActive_thenResultIsAsExpected() =
        runTest {
            val account = createFakeEntity(active = true)
            every { query.executeAsOneOrNull() } returns account

            val res = sut.getActive()

            assertNotNull(res)
            verify {
                queries.getActive()
            }
        }

    @Test
    fun givenNoAccount_whenGetBy_thenResultIsAsExpected() =
        runTest {
            val username = "username"
            val instance = "instance"
            every { query.executeAsOneOrNull() } returns null

            val res = sut.getBy(username, instance)

            assertNull(res)
            verify {
                queries.getBy(username, instance)
            }
        }

    @Test
    fun givenAccount_whenGetBy_thenResultIsAsExpected() =
        runTest {
            val username = "username"
            val instance = "instance"
            val account = createFakeEntity(active = true, username = username, instance = instance)
            every { query.executeAsOneOrNull() } returns account

            val res = sut.getBy(username, instance)

            assertNotNull(res)
            verify {
                queries.getBy(username, instance)
            }
        }

    @Test
    fun whenCreate_thenIdIsReturned() =
        runTest {
            val username = "username"
            val instance = "instance"
            val jwt = "jwt"
            val account = AccountModel(username = username, instance = instance, jwt = jwt)
            every { query.executeAsList() } returns listOf(createFakeEntity(id = 1, jwt = jwt))

            val id = sut.createAccount(account)

            assertEquals(1, id)

            verify {
                queries.create(username, instance, jwt, null)
            }
        }

    @Test
    fun whenSetActive_thenResultIsAsExpected() =
        runTest {
            sut.setActive(id = 1, active = true)

            verify {
                queries.setActive(id = 1)
            }
        }

    @Test
    fun whenSetInactive_thenInteractionsAreAsExpected() =
        runTest {
            sut.setActive(id = 1, active = false)

            verify {
                queries.setInactive(id = 1)
            }
        }

    @Test
    fun whenUpdate_thenInteractionsAreAsExpected() =
        runTest {
            val jwt = "jwt"
            val avatar = "avatar"
            sut.update(1, avatar = avatar, jwt = jwt)

            verify {
                queries.update(jwt = jwt, avatar = avatar, id = 1)
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

    private fun createFakeEntity(
        id: Long = 0,
        active: Boolean = false,
        username: String = "",
        instance: String = "",
        jwt: String? = null,
        avatar: String? = null,
    ) = AccountEntity(
        id = id,
        active = if (active) 1 else 0,
        username = username,
        instance = instance,
        jwt = jwt,
        avatar = avatar,
    )
}
