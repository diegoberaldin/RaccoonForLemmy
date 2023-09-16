package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation.DefaultNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.comments.UserCommentsMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.comments.UserCommentsViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.posts.UserPostsMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.posts.UserPostsViewModel
import org.koin.dsl.module

val commonUiModule = module {
    single<NavigationCoordinator> {
        DefaultNavigationCoordinator()
    }
    factory { params ->
        PostDetailViewModel(
            mvi = DefaultMviModel(PostDetailMviModel.UiState()),
            post = params[0],
            identityRepository = get(),
            siteRepository = get(),
            postsRepository = get(),
            commentRepository = get(),
            keyStore = get(),
            notificationCenter = get(),
            hapticFeedback = get(),
        )
    }
    factory { params ->
        CommunityDetailViewModel(
            mvi = DefaultMviModel(CommunityDetailMviModel.UiState()),
            community = params[0],
            otherInstance = params[1],
            identityRepository = get(),
            communityRepository = get(),
            postsRepository = get(),
            keyStore = get(),
            hapticFeedback = get(),
        )
    }
    factory { params ->
        CommunityInfoViewModel(
            mvi = DefaultMviModel(CommunityInfoMviModel.UiState()),
            community = params[0],
        )
    }
    factory {
        UserDetailViewModel(
            mvi = DefaultMviModel(UserDetailMviModel.UiState()),
            keyStore = get(),
        )
    }
    factory {
        UserPostsViewModel(
            mvi = DefaultMviModel(UserPostsMviModel.UiState()),
            user = it[0],
            identityRepository = get(),
            userRepository = get(),
            postsRepository = get(),
            hapticFeedback = get(),
            keyStore = get(),
            notificationCenter = get(),
        )
    }
    factory {
        UserCommentsViewModel(
            mvi = DefaultMviModel(UserCommentsMviModel.UiState()),
            user = it[0],
            identityRepository = get(),
            userRepository = get(),
            commentRepository = get(),
            hapticFeedback = get(),
            notificationCenter = get(),
        )
    }
    factory {
        InstanceInfoViewModel(
            mvi = DefaultMviModel(InstanceInfoMviModel.UiState()),
            url = it[0],
            siteRepository = get(),
            communityRepository = get(),
            identityRepository = get(),
        )
    }
    factory { params ->
        CreateCommentViewModel(
            mvi = DefaultMviModel(CreateCommentMviModel.UiState()),
            postId = params[0],
            parentId = params[1],
            identityRepository = get(),
            commentRepository = get(),
        )
    }
    factory { params ->
        CreatePostViewModel(
            mvi = DefaultMviModel(CreatePostMviModel.UiState()),
            communityId = params[0],
            identityRepository = get(),
            postsRepository = get(),
        )
    }
}
