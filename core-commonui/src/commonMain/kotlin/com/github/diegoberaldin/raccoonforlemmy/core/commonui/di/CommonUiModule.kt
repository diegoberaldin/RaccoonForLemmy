package com.github.diegoberaldin.raccoonforlemmy.core.commonui.di

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.lemmyUiModule
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.navigationModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.imagePreloadModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.utilsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.di.banModule
import com.github.diegoberaldin.raccoonforlemmy.unit.chat.di.chatModule
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di.communityInfoModule
import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.di.createPostModule
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.di.createReportModule
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.di.drawerModule
import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.di.instanceInfoModule
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.di.postDetailModule
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
        createReportModule,
        createPostModule,
        postDetailModule,
    )
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
}
