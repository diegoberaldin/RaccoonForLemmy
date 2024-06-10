package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository

interface GetSiteSupportsHiddenPostsUseCase {
    suspend operator fun invoke(): Boolean
}
