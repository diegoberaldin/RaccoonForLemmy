package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

internal class DefaultGetSortTypesUseCase(
    private val siteRepository: SiteRepository,
) : GetSortTypesUseCase {
    companion object {
        private const val THRESHOLD_MAJOR = 0
        private const val THRESHOLD_MINOR = 19
        private val LEMMY_VERSION_REGEX = Regex("(?<major>\\d+).(?<minor>\\d+)(.(?<patch>\\d+))?")
    }

    override suspend fun getTypesForPosts(otherInstance: String?): List<SortType> {
        val version = siteRepository.getSiteVersion(otherInstance = otherInstance).orEmpty()
        val matchResult = LEMMY_VERSION_REGEX.find(version)
        val major = matchResult?.groups?.get("major")?.value?.toIntOrNull() ?: 0
        val minor = matchResult?.groups?.get("minor")?.value?.toIntOrNull() ?: 0
        return buildList {
            this += SortType.Active
            this += SortType.Hot
            this += SortType.New
            this += SortType.NewComments
            this += SortType.MostComments
            this += SortType.Old
            if (major >= THRESHOLD_MAJOR && minor >= THRESHOLD_MINOR) {
                this += SortType.Controversial
                this += SortType.Scaled
            }
            this += SortType.Top.Generic
        }
    }

    override suspend fun getTypesForComments(otherInstance: String?): List<SortType> {
        val version = siteRepository.getSiteVersion(otherInstance = otherInstance).orEmpty()
        val matchResult = LEMMY_VERSION_REGEX.find(version)
        val major = matchResult?.groups?.get("major")?.value?.toIntOrNull() ?: 0
        val minor = matchResult?.groups?.get("minor")?.value?.toIntOrNull() ?: 0
        return buildList {
            this += SortType.Hot
            this += SortType.New
            this += SortType.Old
            if (major >= THRESHOLD_MAJOR && minor >= THRESHOLD_MINOR) {
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

    override suspend fun getTypesForSavedItems(): List<SortType> =
        buildList {
            this += SortType.Hot
            this += SortType.New
            this += SortType.Old
        }
}
