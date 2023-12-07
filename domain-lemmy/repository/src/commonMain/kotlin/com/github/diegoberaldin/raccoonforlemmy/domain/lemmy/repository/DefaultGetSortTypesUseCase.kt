package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

internal class DefaultGetSortTypesUseCase(
    private val siteRepository: SiteRepository
) : GetSortTypesUseCase {

    companion object {
        private const val LEMMY_VERSION_CONTROVERSIAL = "0.19"
        private const val LEMMY_VERSION_SCALED = "0.19"
    }

    override suspend fun getTypesForPosts(otherInstance: String?): List<SortType> {
        val version = siteRepository.getSiteVersion(otherInstance = otherInstance).orEmpty()
        return buildList {
            this += SortType.Active
            this += SortType.Hot
            this += SortType.New
            this += SortType.NewComments
            this += SortType.MostComments
            this += SortType.Old
            if (version.startsWith(LEMMY_VERSION_CONTROVERSIAL)) {
                this += SortType.Controversial
            }
            if (version.startsWith(LEMMY_VERSION_SCALED)) {
                this += SortType.Scaled
            }
            this += SortType.Top.Generic
        }
    }

    override suspend fun getTypesForComments(otherInstance: String?): List<SortType> {
        val version = siteRepository.getSiteVersion(otherInstance = otherInstance).orEmpty()
        return buildList {
            this += SortType.Hot
            this += SortType.New
            this += SortType.Old
            if (version.startsWith(LEMMY_VERSION_CONTROVERSIAL)) {
                this += SortType.Controversial
            }
            this += SortType.Top.Generic
        }
    }

    override suspend fun getTypesForCommunities(otherInstance: String?): List<SortType> =
        buildList {
            this += SortType.Active
            this += SortType.New
            this += SortType.MostComments
        }

    override suspend fun getTypesForSavedItems(): List<SortType> = buildList {
        this += SortType.Hot
        this += SortType.New
        this += SortType.Old
    }
}