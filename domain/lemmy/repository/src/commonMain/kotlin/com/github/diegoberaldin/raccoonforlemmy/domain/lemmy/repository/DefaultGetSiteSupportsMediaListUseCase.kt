package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

internal class DefaultGetSiteSupportsMediaListUseCase(
    private val isSiteVersionAtLeastUseCase: IsSiteVersionAtLeastUseCase,
) : GetSiteSupportsMediaListUseCase {
    companion object {
        const val THRESHOLD_MAJOR = 0
        const val THRESHOLD_MINOR = 19
        const val THRESHOLD_PATCH = 4
    }

    override suspend fun invoke(): Boolean =
        isSiteVersionAtLeastUseCase.execute(
            major = THRESHOLD_MAJOR,
            minor = THRESHOLD_MINOR,
            patch = THRESHOLD_PATCH,
        )
}
