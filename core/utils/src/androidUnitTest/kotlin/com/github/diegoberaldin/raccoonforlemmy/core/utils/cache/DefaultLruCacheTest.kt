package com.github.diegoberaldin.raccoonforlemmy.core.utils.cache

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DefaultLruCacheTest {

    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val sut = DefaultLruCache<Any>(size = 3)

    @Test
    fun givenNotExistingKey_whenGet_thenResultIsAsExpected() = runTest {
        val res = sut.get(1)

        assertNull(res)
    }

    @Test
    fun givenExistingKey_whenGet_thenResultIsAsExpected() = runTest {
        sut.put(value = "test", key = 1)
        val res = sut.get(1)

        assertEquals("test", res)
    }

    @Test
    fun givenSizeExceeded_whenGet_thenResultIsAsExpected() = runTest {
        sut.put(value = "test_1", key = 1)
        sut.put(value = "test_2", key = 2)
        sut.put(value = "test_3", key = 3)
        sut.put(value = "test_4", key = 4)
        val res1 = sut.get(1)

        assertNull(res1)

        val res2 = sut.get(2)
        assertEquals("test_2", res2)
    }
}
