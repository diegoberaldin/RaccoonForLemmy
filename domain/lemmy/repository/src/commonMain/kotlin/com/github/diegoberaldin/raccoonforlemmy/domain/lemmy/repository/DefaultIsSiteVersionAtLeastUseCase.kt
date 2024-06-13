package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

internal class DefaultIsSiteVersionAtLeastUseCase(
    private val siteRepository: SiteRepository,
) : IsSiteVersionAtLeastUseCase {
    companion object {
        private val LEMMY_VERSION_REGEX = Regex("(?<major>\\d+).(?<minor>\\d+)(.(?<patch>\\d+))?")
    }

    override suspend fun execute(
        major: Int,
        minor: Int,
        patch: Int,
        otherInstance: String?,
    ): Boolean {
        val version = siteRepository.getSiteVersion(otherInstance = otherInstance).orEmpty()
        val matchResult = LEMMY_VERSION_REGEX.find(version)
        val actualMajor = matchResult?.groups?.get("major")?.value?.toIntOrNull() ?: 0
        val actualMinor = matchResult?.groups?.get("minor")?.value?.toIntOrNull() ?: 0
        val actualPatch = matchResult?.groups?.get("patch")?.value?.toIntOrNull() ?: 0
        return when {
            actualMajor < major -> false
            actualMajor > major -> true
            actualMinor < minor -> false
            actualMinor > minor -> true
            actualPatch < patch -> false
            else -> true
        }
    }
}
