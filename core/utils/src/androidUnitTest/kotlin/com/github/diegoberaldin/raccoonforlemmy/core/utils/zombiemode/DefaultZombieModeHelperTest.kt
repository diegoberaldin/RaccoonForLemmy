package com.github.diegoberaldin.raccoonforlemmy.core.utils.zombiemode

import app.cash.turbine.test
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
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
            dispatcher = Dispatchers.Default,
        )
    private val interval = 1.seconds

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun whenStartThenIndexIsAsExpected() =
        runTest {
            sut.start(initialValue = 0, interval = interval)
            sut.index.test {
                val i0 = awaitItem()
                assertEquals(0, i0)
                advanceTimeBy(2.seconds)
                val i1 = awaitItem()
                assertEquals(1, i1)
            }
        }

    @Test
    fun whenPauseThenIndexIsAsExpected() =
        runTest {
            sut.start(initialValue = 0, interval = interval)
            sut.index.test {
                val i0 = awaitItem()
                assertEquals(0, i0)
                sut.pause()
                val i1 = awaitItem()
                assertEquals(-1, i1)
                expectNoEvents()
            }
        }
}
