package com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository

import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DefaultCommunitySortRepositoryTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val keyStore = mockk<TemporaryKeyStore>(relaxUnitFun = true)

    private val sut = DefaultCommunitySortRepository(keyStore = keyStore)

    @Test
    fun givenEmptyInitialState_whenSaveSort_thenValueIsStored() {
        every { keyStore.get("communitySort", listOf()) } returns listOf()

        sut.saveSort("!raccoonforlemmy@lemmy.world", 1)

        verify {
            keyStore.save("communitySort", listOf("!raccoonforlemmy@lemmy.world:1"))
        }
    }

    @Test
    fun givenCommunityAlreadyExisting_whenSaveSort_thenValueIsStored() {
        every { keyStore.get("communitySort", listOf()) } returns listOf("!raccoonforlemmy@lemmy.world:0")

        sut.saveSort("!raccoonforlemmy@lemmy.world", 1)

        verify {
            keyStore.save("communitySort", listOf("!raccoonforlemmy@lemmy.world:1"))
        }
    }

    @Test
    fun givenOtherCommunityAlreadyExisting_whenSaveSort_thenBothValuesAreStored() {
        every { keyStore.get("communitySort", listOf()) } returns listOf("!test@lemmy.world:1")

        sut.saveSort("!raccoonforlemmy@lemmy.world", 1)

        verify {
            keyStore.save("communitySort", listOf("!test@lemmy.world:1", "!raccoonforlemmy@lemmy.world:1"))
        }
    }

    @Test
    fun givenEmptyInitialState_whenGet_thenResultIsAsExpected() {
        every { keyStore.get("communitySort", listOf()) } returns listOf()

        val res = sut.getSort("!raccoonforlemmy@lemmy.world")

        assertNull(res)
    }

    @Test
    fun givenCommunityExists_whenGet_thenResultIsAsExpected() {
        every { keyStore.get("communitySort", listOf()) } returns listOf("!raccoonforlemmy@lemmy.world:2")

        val res = sut.getSort("!raccoonforlemmy@lemmy.world")

        assertEquals(2, res)
    }

    @Test
    fun givenCommunityDoesNotExist_whenGet_thenResultIsAsExpected() {
        every { keyStore.get("communitySort", listOf()) } returns listOf("!test@lemmy.world:2")

        val res = sut.getSort("!raccoonforlemmy@lemmy.world")

        assertNull(res)
    }
}
