package com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.di

import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.DefaultCommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.DefaultCommunityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.DefaultGetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.DefaultModlogRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.DefaultPostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.DefaultPrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.DefaultSiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.DefaultUserRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.ModlogRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single<GetSortTypesUseCase> {
        DefaultGetSortTypesUseCase(
            siteRepository = get()
        )
    }
    single<PostRepository> {
        DefaultPostRepository(
            services = get(named("default")),
            customServices = get(named("custom")),
        )
    }
    single<CommunityRepository> {
        DefaultCommunityRepository(
            services = get(named("default")),
            customServices = get(named("custom")),
        )
    }
    single<UserRepository> {
        DefaultUserRepository(
            services = get(named("default")),
            customServices = get(named("custom")),
        )
    }
    single<SiteRepository> {
        DefaultSiteRepository(
            services = get(named("default")),
            customServices = get(named("custom")),
        )
    }
    single<CommentRepository> {
        DefaultCommentRepository(
            services = get(named("default")),
            customServices = get(named("custom")),
        )
    }
    single<PrivateMessageRepository> {
        DefaultPrivateMessageRepository(
            services = get(named("default")),
        )
    }
    single<ModlogRepository> {
        DefaultModlogRepository(
            services = get(named("default")),
        )
    }
}
