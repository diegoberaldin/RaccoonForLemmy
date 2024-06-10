package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.core.testutils.DispatcherTestRule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultGetSortTypesUseCaseTest {
    @get:Rule
    val dispatcherTestRule = DispatcherTestRule()

    private val isSiteVersionAtLeastUseCase: IsSiteVersionAtLeastUseCase = mockk()

    private val sut =
        DefaultGetSortTypesUseCase(
            isSiteVersionAtLeastUseCase = isSiteVersionAtLeastUseCase,
        )

    @Test
    fun givenVersionEqualToThreshold_whenGetTypesForPosts_thenResultsIsAsExpected() =
        runTest {
            coEvery {
                isSiteVersionAtLeastUseCase.execute(any(), any(), any(), any())
            } returns true

            val res = sut.getTypesForPosts()

            assertTrue(res.contains(SortType.Controversial))
            assertTrue(res.contains(SortType.Scaled))
            assertTrue(res.contains(SortType.Active))
            assertTrue(res.contains(SortType.Hot))
            assertTrue(res.contains(SortType.New))
            assertTrue(res.contains(SortType.Old))
            assertTrue(res.contains(SortType.MostComments))
            assertTrue(res.contains(SortType.NewComments))
            assertTrue(res.contains(SortType.Top.Generic))
        }

    @Test
    fun givenVersionGreaterThanThreshold_whenGetTypesForPosts_thenResultsIsAsExpected() =
        runTest {
            coEvery {
                isSiteVersionAtLeastUseCase.execute(any(), any(), any(), any())
            } returns true

            val res = sut.getTypesForPosts()

            assertTrue(res.contains(SortType.Controversial))
            assertTrue(res.contains(SortType.Scaled))
            assertTrue(res.contains(SortType.Active))
            assertTrue(res.contains(SortType.Hot))
            assertTrue(res.contains(SortType.New))
            assertTrue(res.contains(SortType.Old))
            assertTrue(res.contains(SortType.MostComments))
            assertTrue(res.contains(SortType.NewComments))
            assertTrue(res.contains(SortType.Top.Generic))
        }

    @Test
    fun givenVersionLessThanThreshold_whenGetTypesForPosts_thenResultsIsAsExpected() =
        runTest {
            coEvery {
                isSiteVersionAtLeastUseCase.execute(any(), any(), any(), any())
            } returns false

            val res = sut.getTypesForPosts()

            assertFalse(res.contains(SortType.Controversial))
            assertFalse(res.contains(SortType.Scaled))
            assertTrue(res.contains(SortType.Active))
            assertTrue(res.contains(SortType.Hot))
            assertTrue(res.contains(SortType.New))
            assertTrue(res.contains(SortType.Old))
            assertTrue(res.contains(SortType.MostComments))
            assertTrue(res.contains(SortType.NewComments))
            assertTrue(res.contains(SortType.Top.Generic))
        }

    @Test
    fun givenVersionGreaterThanThreshold_whenGetTypesForComments_thenResultsIsAsExpected() =
        runTest {
            coEvery {
                isSiteVersionAtLeastUseCase.execute(any(), any(), any(), any())
            } returns true

            val res = sut.getTypesForComments()

            assertTrue(res.contains(SortType.Controversial))
            assertTrue(res.contains(SortType.Hot))
            assertTrue(res.contains(SortType.New))
            assertTrue(res.contains(SortType.Old))
            assertTrue(res.contains(SortType.Top.Generic))
        }

    @Test
    fun givenVersionLessThanThreshold_whenGetTypesForComments_thenResultsIsAsExpected() =
        runTest {
            coEvery {
                isSiteVersionAtLeastUseCase.execute(any(), any(), any(), any())
            } returns false

            val res = sut.getTypesForComments()

            assertFalse(res.contains(SortType.Controversial))
            assertTrue(res.contains(SortType.Hot))
            assertTrue(res.contains(SortType.New))
            assertTrue(res.contains(SortType.Old))
            assertTrue(res.contains(SortType.Top.Generic))
        }

    @Test
    fun whenGetTypesForCommunities_thenResultsIsAsExpected() =
        runTest {
            val res = sut.getTypesForCommunities()

            assertTrue(res.contains(SortType.Active))
            assertTrue(res.contains(SortType.New))
            assertTrue(res.contains(SortType.MostComments))
        }

    @Test
    fun whenGetTypesForSavedItems_thenResultsIsAsExpected() =
        runTest {
            val res = sut.getTypesForSavedItems()

            assertTrue(res.contains(SortType.Hot))
            assertTrue(res.contains(SortType.New))
            assertTrue(res.contains(SortType.Old))
        }
}
