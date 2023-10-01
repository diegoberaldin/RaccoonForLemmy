package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat.InboxChatMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat.InboxChatViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation.DefaultNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailViewModel
import org.koin.dsl.module

val commonUiModule = module {
    single<NavigationCoordinator> {
        DefaultNavigationCoordinator()
    }
    factory { params ->
        PostDetailViewModel(
            mvi = DefaultMviModel(PostDetailMviModel.UiState()),
            post = params[0],
            highlightCommentId = params[1],
            identityRepository = get(),
            siteRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
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
            postRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            hapticFeedback = get(),
        )
    }
    factory { params ->
        CommunityInfoViewModel(
            mvi = DefaultMviModel(CommunityInfoMviModel.UiState()),
            community = params[0],
        )
    }
    factory { params ->
        UserDetailViewModel(
            mvi = DefaultMviModel(UserDetailMviModel.UiState()),
            user = params[0],
            otherInstance = params[1],
            identityRepository = get(),
            userRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
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
            editedCommentId = params[2],
            identityRepository = get(),
            commentRepository = get(),
            postRepository = get(),
            themeRepository = get(),
            notificationCenter = get(),
        )
    }
    factory { params ->
        CreatePostViewModel(
            mvi = DefaultMviModel(CreatePostMviModel.UiState()),
            communityId = params[0],
            editedPostId = params[1],
            identityRepository = get(),
            postRepository = get(),
        )
    }
    factory {
        ZoomableImageViewModel(
            mvi = DefaultMviModel(ZoomableImageMviModel.UiState()),
            shareHelper = get(),
            galleryHelper = get(),
        )
    }
    factory { params ->
        InboxChatViewModel(
            otherUserId = params[0],
            mvi = DefaultMviModel(InboxChatMviModel.UiState()),
            identityRepository = get(),
            siteRepository = get(),
            userRepository = get(),
            messageRepository = get(),
            notificationCenter = get(),
        )
    }
}
