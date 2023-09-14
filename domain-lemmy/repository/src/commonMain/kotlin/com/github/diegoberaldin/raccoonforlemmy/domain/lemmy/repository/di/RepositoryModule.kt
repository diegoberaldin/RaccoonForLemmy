package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single {
        PostsRepository(
            services = get(),
            customServices = get(named("custom")),
        )
    }
    single {
        CommunityRepository(
            services = get(),
            customServices = get(named("custom")),
        )
    }
    singleOf(::UserRepository)
    singleOf(::SiteRepository)
    singleOf(::CommentRepository)
    singleOf(::PrivateMessageRepository)
}
