package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

interface GetSiteSupportsMediaListUseCase {
    suspend operator fun invoke(): Boolean
}
