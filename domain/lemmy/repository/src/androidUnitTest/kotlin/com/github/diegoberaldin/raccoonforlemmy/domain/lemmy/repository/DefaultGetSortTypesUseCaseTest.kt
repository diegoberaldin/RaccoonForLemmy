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

    private val siteRepository: SiteRepository = mockk(relaxUnitFun = true)

    private val sut = DefaultGetSortTypesUseCase(
        siteRepository = siteRepository,
    )

    @Test
    fun givenVersionEqualTo019_whenGetTypesForPosts_thenResultsIsAsExpected() = runTest {
        coEvery {
            siteRepository.getSiteVersion(
                auth = any(),
                otherInstance = any()
            )
        } returns "0.19"

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
    fun givenVersionGreaterThan019WithPatch_whenGetTypesForPosts_thenResultsIsAsExpected() = runTest {
        coEvery {
            siteRepository.getSiteVersion(
                auth = any(),
                otherInstance = any()
            )
        } returns "0.19.2"

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
    fun givenVersionGreaterThan019WithMinor_whenGetTypesForPosts_thenResultsIsAsExpected() = runTest {
        coEvery {
            siteRepository.getSiteVersion(
                auth = any(),
                otherInstance = any()
            )
        } returns "0.20"

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
    fun givenVersionLessThan019_whenGetTypesForPosts_thenResultsIsAsExpected() = runTest {
        coEvery {
            siteRepository.getSiteVersion(
                auth = any(),
                otherInstance = any()
            )
        } returns "0.18.5"

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
    fun givenVersionEqualTo019_whenGetTypesForComments_thenResultsIsAsExpected() = runTest {
        coEvery {
            siteRepository.getSiteVersion(
                auth = any(),
                otherInstance = any()
            )
        } returns "0.19"

        val res = sut.getTypesForComments()

        assertTrue(res.contains(SortType.Controversial))
        assertTrue(res.contains(SortType.Hot))
        assertTrue(res.contains(SortType.New))
        assertTrue(res.contains(SortType.Old))
        assertTrue(res.contains(SortType.Top.Generic))
    }

    @Test
    fun givenVersionGreaterThan019WithPatch_whenGetTypesForComments_thenResultsIsAsExpected() = runTest {
        coEvery {
            siteRepository.getSiteVersion(
                auth = any(),
                otherInstance = any()
            )
        } returns "0.19.2"

        val res = sut.getTypesForComments()

        assertTrue(res.contains(SortType.Controversial))
        assertTrue(res.contains(SortType.Hot))
        assertTrue(res.contains(SortType.New))
        assertTrue(res.contains(SortType.Old))
        assertTrue(res.contains(SortType.Top.Generic))
    }

    @Test
    fun givenVersionGreaterThan019WithMinor_whenGetTypesForComments_thenResultsIsAsExpected() = runTest {
        coEvery {
            siteRepository.getSiteVersion(
                auth = any(),
                otherInstance = any()
            )
        } returns "0.20"

        val res = sut.getTypesForPosts()

        assertTrue(res.contains(SortType.Controversial))
        assertTrue(res.contains(SortType.Hot))
        assertTrue(res.contains(SortType.New))
        assertTrue(res.contains(SortType.Old))
        assertTrue(res.contains(SortType.Top.Generic))
    }

    @Test
    fun givenVersionLessThan019_whenGetTypesForComments_thenResultsIsAsExpected() = runTest {
        coEvery {
            siteRepository.getSiteVersion(
                auth = any(),
                otherInstance = any()
            )
        } returns "0.18.5"

        val res = sut.getTypesForComments()

        assertFalse(res.contains(SortType.Controversial))
        assertTrue(res.contains(SortType.Hot))
        assertTrue(res.contains(SortType.New))
        assertTrue(res.contains(SortType.Old))
        assertTrue(res.contains(SortType.Top.Generic))
    }

    @Test
    fun whenGetTypesForCommunities_thenResultsIsAsExpected() = runTest {
        val res = sut.getTypesForCommunities()

        assertTrue(res.contains(SortType.Active))
        assertTrue(res.contains(SortType.New))
        assertTrue(res.contains(SortType.MostComments))
    }

    @Test
    fun whenGetTypesForSavedItems_thenResultsIsAsExpected() = runTest {
        val res = sut.getTypesForSavedItems()

        assertTrue(res.contains(SortType.Hot))
        assertTrue(res.contains(SortType.New))
        assertTrue(res.contains(SortType.Old))
    }
}