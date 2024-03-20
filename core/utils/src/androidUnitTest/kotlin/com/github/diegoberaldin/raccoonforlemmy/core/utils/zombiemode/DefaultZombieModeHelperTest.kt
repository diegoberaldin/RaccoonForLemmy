package com.github.diegoberaldin.raccoonforlemmy.core.utils.zombiemode

import app.cash.turbine.test
import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
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

    private val sut = DefaultZombieModeHelper()
    private val interval = 1.seconds

    @Test
    fun whenStartThenEventsAreEmitted() = runTest {
        sut.start(initialValue = 0, interval = interval)
        sut.index.test {
            val item = awaitItem()
            assertEquals(0, item)
            val secondItem = awaitItem()
            assertEquals(1, secondItem)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun whenPauseThenIndexIsReset() = runTest {
        sut.start(initialValue = 0, interval = interval)
        sut.index.test {
            val item = awaitItem()
            assertEquals(0, item)
            sut.pause()
            val secondItem = awaitItem()
            assertEquals(-1, secondItem)
            advanceTimeBy(interval)
            assertEquals(-1, sut.index.value)
        }
    }
}