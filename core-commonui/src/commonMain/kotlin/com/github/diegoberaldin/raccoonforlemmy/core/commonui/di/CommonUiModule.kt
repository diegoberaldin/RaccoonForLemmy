package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat.InboxChatMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat.InboxChatViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communityInfo.CommunityInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.DefaultFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createreport.CreateReportMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createreport.CreateReportViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.DefaultDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.DrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.ModalDrawerMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.ModalDrawerViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo.InstanceInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation.DefaultNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.navigation.NavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.remove.RemoveMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.remove.RemoveViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.reportlist.ReportListMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.reportlist.ReportListViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems.SavedItemsMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems.SavedItemsViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.selectcommunity.SelectCommunityMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.selectcommunity.SelectCommunityViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.utilsModule
import org.koin.dsl.module

val commonUiModule = module {
    includes(
        utilsModule,
        imagePreloadModule,
    )

    single<NavigationCoordinator> {
        DefaultNavigationCoordinator()
    }
    single<DrawerCoordinator> {
        DefaultDrawerCoordinator()
    }
    factory<FabNestedScrollConnection> {
        DefaultFabNestedScrollConnection()
    }
    factory<PostDetailMviModel> { params ->
        PostDetailViewModel(
            mvi = DefaultMviModel(PostDetailMviModel.UiState()),
            post = params[0],
            otherInstance = params[1],
            highlightCommentId = params[2],
            isModerator = params[3],
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
    factory<CommunityDetailMviModel> { params ->
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
            zombieModeHelper = get(),
            imagePreloadManager = get(),
            notificationCenter = get(),
        )
    }
    factory<CommunityInfoMviModel> { params ->
        CommunityInfoViewModel(
            mvi = DefaultMviModel(CommunityInfoMviModel.UiState()),
            community = params[0],
        )
    }
    factory<UserDetailMviModel> { params ->
        UserDetailViewModel(
            mvi = DefaultMviModel(UserDetailMviModel.UiState()),
            user = params[0],
            otherInstance = params[1],
            identityRepository = get(),
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
        )
    }
    factory<InstanceInfoMviModel> {
        InstanceInfoViewModel(
            mvi = DefaultMviModel(InstanceInfoMviModel.UiState()),
            url = it[0],
            siteRepository = get(),
            communityRepository = get(),
            identityRepository = get(),
            settingsRepository = get(),
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
            themeRepository = get(),
            settingsRepository = get(),
        )
    }
    factory<ZoomableImageMviModel> {
        ZoomableImageViewModel(
            mvi = DefaultMviModel(ZoomableImageMviModel.UiState()),
            shareHelper = get(),
            galleryHelper = get(),
            settingsRepository = get(),
        )
    }
    factory<InboxChatMviModel> { params ->
        InboxChatViewModel(
            otherUserId = params[0],
            mvi = DefaultMviModel(InboxChatMviModel.UiState()),
            identityRepository = get(),
            siteRepository = get(),
            userRepository = get(),
            messageRepository = get(),
            notificationCenter = get(),
            settingsRepository = get(),
        )
    }
    factory<SavedItemsMviModel> {
        SavedItemsViewModel(
            mvi = DefaultMviModel(SavedItemsMviModel.UiState()),
            identityRepository = get(),
            siteRepository = get(),
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
    factory<ModalDrawerMviModel> {
        ModalDrawerViewModel(
            mvi = DefaultMviModel(ModalDrawerMviModel.UiState()),
            apiConfigurationRepository = get(),
            siteRepository = get(),
            identityRepository = get(),
            accountRepository = get(),
            communityRepository = get(),
            multiCommunityRepository = get(),
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
    factory<SelectCommunityMviModel> {
        SelectCommunityViewModel(
            mvi = DefaultMviModel(SelectCommunityMviModel.UiState()),
            identityRepository = get(),
            communityRepository = get(),
            settingsRepository = get(),
            notificationCenter = get(),
        )
    }
    factory<RemoveMviModel> { params ->
        RemoveViewModel(
            postId = params[0],
            commentId = params[1],
            mvi = DefaultMviModel(RemoveMviModel.UiState()),
            identityRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            notificationCenter = get(),
        )
    }
    factory<ReportListMviModel> { params ->
        ReportListViewModel(
            communityId = params[0],
            mvi = DefaultMviModel(ReportListMviModel.UiState()),
            identityRepository = get(),
            postRepository = get(),
            commentRepository = get(),
            themeRepository = get(),
            settingsRepository = get(),
            hapticFeedback = get(),
        )
    }
}
