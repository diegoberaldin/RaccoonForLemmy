package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DefaultCommunityPreferredLanguageRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val keyStore = mockk<TemporaryKeyStore>(relaxUnitFun = true)

    private val sut = DefaultCommunityPreferredLanguageRepository(keyStore = keyStore)

    @Test
    fun givenEmptyInitialState_whenSave_thenValueIsStored() = runTest {
        every { keyStore.get(KEY, listOf()) } returns listOf()

        sut.save("!raccoonforlemmy@lemmy.world", 1)

        verify {
            keyStore.save(KEY, listOf("!raccoonforlemmy@lemmy.world:1"))
        }
    }

    @Test
    fun givenCommunityAlreadyExisting_whenSave_thenValueIsStored() = runTest {
        every { keyStore.get(KEY, listOf()) } returns listOf("!raccoonforlemmy@lemmy.world:0")

        sut.save("!raccoonforlemmy@lemmy.world", 1)

        verify {
            keyStore.save(KEY, listOf("!raccoonforlemmy@lemmy.world:1"))
        }
    }

    @Test
    fun givenCommunityAlreadyExisting_whenSaveNull_thenValueIsRemoved() = runTest {
        every { keyStore.get(KEY, listOf()) } returns listOf("!raccoonforlemmy@lemmy.world:0")

        sut.save("!raccoonforlemmy@lemmy.world", null)

        verify {
            keyStore.save(KEY, emptyList())
        }
    }

    @Test
    fun givenOtherCommunityAlreadyExisting_whenSave_thenBothValuesAreStored() = runTest {
        every { keyStore.get(KEY, listOf()) } returns listOf("!test@lemmy.world:1")

        sut.save("!raccoonforlemmy@lemmy.world", 1)

        verify {
            keyStore.save(KEY, listOf("!test@lemmy.world:1", "!raccoonforlemmy@lemmy.world:1"))
        }
    }

    @Test
    fun givenOtherCommunityAlreadyExisting_whenSaveNull_thenValueIsRemovedButTheOtherIsNot() =
        runTest {
            every { keyStore.get(KEY, listOf()) } returns listOf("!test@lemmy.world:1")

            sut.save("!raccoonforlemmy@lemmy.world", null)

            verify {
                keyStore.save(KEY, listOf("!test@lemmy.world:1"))
            }
        }

    @Test
    fun givenEmptyInitialState_whenGet_thenResultIsAsExpected() = runTest {
        every { keyStore.get(KEY, listOf()) } returns listOf()

        val res = sut.get("!raccoonforlemmy@lemmy.world")

        assertNull(res)
    }

    @Test
    fun givenCommunityExists_whenGet_thenResultIsAsExpected() = runTest {
        every { keyStore.get(KEY, listOf()) } returns listOf("!raccoonforlemmy@lemmy.world:2")

        val res = sut.get("!raccoonforlemmy@lemmy.world")

        assertEquals(2, res)
    }

    @Test
    fun givenCommunityDoesNotExist_whenGet_thenResultIsAsExpected() = runTest {
        every { keyStore.get(KEY, listOf()) } returns listOf("!test@lemmy.world:2")

        val res = sut.get("!raccoonforlemmy@lemmy.world")

        assertNull(res)
    }

    companion object {
        private const val KEY = "communityPreferredLanguage"
    }
}
