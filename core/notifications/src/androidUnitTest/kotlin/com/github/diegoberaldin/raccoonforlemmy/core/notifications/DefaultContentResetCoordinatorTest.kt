package com.github.diegoberaldin.raccoonforlemmy.core.notifications

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultContentResetCoordinatorTest {

    @get:Rule
    val dispatcherRule = DispatcherTestRule()

    private val sut = DefaultContentResetCoordinator()

    @Test
    fun whenResetExplore_thenValueIsUpdatedAccordingly() = runTest {
        val initial = sut.resetExplore
        assertFalse(initial)

        with(sut) {
            resetExplore = true
        }

        assertTrue(sut.resetExplore)
    }

    @Test
    fun whenResetHome_thenValueIsUpdatedAccordingly() = runTest {
        val initial = sut.resetHome
        assertFalse(initial)

        with(sut) {
            resetHome = true
        }

        assertTrue(sut.resetHome)
    }

    @Test
    fun whenResetInbox_thenValueIsUpdatedAccordingly() = runTest {
        val initial = sut.resetInbox
        assertFalse(initial)

        with(sut) {
            resetInbox = true
        }

        assertTrue(sut.resetInbox)
    }
}