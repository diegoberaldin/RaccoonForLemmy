package com.github.diegoberaldin.raccoonforlemmy.core.utils.zombiemode

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class DefaultZombieModeHelperTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val sut =
        DefaultZombieModeHelper(
            dispatcher = dispatcherTestRule.dispatcher,
        )
    private val interval = 1.seconds

    @Test
    fun whenStartThenIndexIsAsExpected() =
        runTest {
            sut.start(initialValue = 0, interval = interval)
            val i0 = sut.index.value
            assertEquals(0, i0)
        }

    @Test
    fun whenPauseThenIndexIsAsExpected() =
        runTest {
            sut.start(initialValue = 0, interval = interval)
            val i0 = sut.index.value
            assertEquals(0, i0)
            sut.pause()
            val i1 = sut.index.value
            assertEquals(-1, i1)
        }
}
