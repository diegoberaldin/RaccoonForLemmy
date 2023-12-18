package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createreport.CreateReportMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createreport.CreateReportViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.lemmyUiModule
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.navigationModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.imagePreloadModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.utilsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.di.banModule
import com.github.diegoberaldin.raccoonforlemmy.unit.chat.di.chatModule
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di.communityInfoModule
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.di.drawerModule
import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.di.instanceInfoModule
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.di.removeModule
import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.di.reportListModule
import com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.di.savedItemsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.di.selectCommunityModule
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.di.zoomableImageModule
import org.koin.dsl.module

val commonUiModule = module {
    includes(
        utilsModule,
        imagePreloadModule,
        navigationModule,
        lemmyUiModule,
        banModule,
        zoomableImageModule,
        chatModule,
        selectCommunityModule,
        drawerModule,
        communityInfoModule,
        instanceInfoModule,
        removeModule,
        reportListModule,
        savedItemsModule,
    )
    factory<PostDetailMviModel> { params ->
        PostDetailViewModel(
            mvi = DefaultMviModel(PostDetailMviModel.UiState()),
            post = params[0],
            otherInstance = params[1],
            highlightCommentId = params[2],
            isModerator = params[3],
            identityRepository = get(),
            apiConfigurationRepository = get(),
            siteRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            communityRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            notificationCenter = get(),
            hapticFeedback = get(),
            getSortTypesUseCase = get(),
        )
    }
    factory<CommunityDetailMviModel> { params ->
        CommunityDetailViewModel(
            mvi = DefaultMviModel(CommunityDetailMviModel.UiState()),
            community = params[0],
            otherInstance = params[1],
            identityRepository = get(),
            apiConfigurationRepository = get(),
            communityRepository = get(),
            postRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            hapticFeedback = get(),
            zombieModeHelper = get(),
            imagePreloadManager = get(),
            notificationCenter = get(),
            getSortTypesUseCase = get(),
        )
    }
    factory<UserDetailMviModel> { params ->
        UserDetailViewModel(
            mvi = DefaultMviModel(UserDetailMviModel.UiState()),
            user = params[0],
            otherInstance = params[1],
            identityRepository = get(),
            apiConfigurationRepository = get(),
            userRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            shareHelper = get(),
            hapticFeedback = get(),
            notificationCenter = get(),
            imagePreloadManager = get(),
            getSortTypesUseCase = get(),
        )
    }
    factory<CreateCommentMviModel> { params ->
        CreateCommentViewModel(
            mvi = DefaultMviModel(CreateCommentMviModel.UiState()),
            postId = params[0],
            parentId = params[1],
            editedCommentId = params[2],
            identityRepository = get(),
            commentRepository = get(),
            postRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            notificationCenter = get(),
        )
    }
    factory<CreatePostMviModel> { params ->
        CreatePostViewModel(
            mvi = DefaultMviModel(CreatePostMviModel.UiState()),
            editedPostId = params[0],
            identityRepository = get(),
            postRepository = get(),
            siteRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
        )
    }
    factory<CreateReportMviModel> { params ->
        CreateReportViewModel(
            postId = params[0],
            commentId = params[1],
            mvi = DefaultMviModel(CreateReportMviModel.UiState()),
            identityRepository = get(),
            postRepository = get(),
            commentRepository = get(),
        )
    }
}
