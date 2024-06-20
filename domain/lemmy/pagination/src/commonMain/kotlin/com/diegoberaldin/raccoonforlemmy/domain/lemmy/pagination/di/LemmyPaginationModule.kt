package com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.di

import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommentPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.CommunityPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.DefaultCommentPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.DefaultCommunityPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.DefaultMultiCommunityPaginator
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.DefaultPostNavigationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.DefaultPostPaginationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.MultiCommunityPaginator
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.PostNavigationManager
import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.PostPaginationManager
import org.koin.dsl.module

val paginationModule =
    module {
        factory<MultiCommunityPaginator> {
            DefaultMultiCommunityPaginator(
                postRepository = get(),
            )
        }
        factory<PostPaginationManager> {
            DefaultPostPaginationManager(
                identityRepository = get(),
                postRepository = get(),
                communityRepository = get(),
                userRepository = get(),
                multiCommunityPaginator = get(),
                notificationCenter = get(),
            )
        }
        factory<CommentPaginationManager> {
            DefaultCommentPaginationManager(
                identityRepository = get(),
                userRepository = get(),
                commentRepository = get(),
                notificationCenter = get(),
            )
        }
        single<PostNavigationManager> {
            DefaultPostNavigationManager(
                postPaginationManager = get(),
            )
        }
        factory<CommunityPaginationManager> {
            DefaultCommunityPaginationManager(
                identityRepository = get(),
                communityRepository = get(),
            )
        }
    }
