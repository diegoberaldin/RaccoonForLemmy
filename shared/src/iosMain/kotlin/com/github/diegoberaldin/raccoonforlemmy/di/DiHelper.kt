package com.github.diegoberaldin.raccoonforlemmy.di

import com.diegoberaldin.raccoonforlemmy.domain.lemmy.pagination.di.paginationModule
import com.github.diegoberaldin.raccoonforlemmy.core.api.di.coreApiModule
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.coreAppearanceModule
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.lemmyUiModule
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.di.coreL10nModule
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.navigationModule
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.coreNotificationModule
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.corePersistenceModule
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.di.corePreferencesModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.AppInfo
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.appIconModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.crashReportModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.customTabsModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.fileSystemModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.galleryHelperModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.hapticFeedbackModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.imagePreloadModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.networkModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.shareHelperModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.di.utilsModule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.di.coreIdentityModule
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.di.domainInboxModule
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.di.repositoryModule
import com.github.diegoberaldin.raccoonforlemmy.feature.home.di.homeTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di.inboxTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.profileTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.searchTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.di.settingsTabModule
import com.github.diegoberaldin.raccoonforlemmy.unit.accountsettings.di.accountSettingsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.di.banModule
import com.github.diegoberaldin.raccoonforlemmy.unit.chat.di.chatModule
import com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail.di.communityDetailModule
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.di.communityInfoModule
import com.github.diegoberaldin.raccoonforlemmy.unit.configurecontentview.di.configureContentViewModule
import com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions.di.configureSwipeActionsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.createcomment.di.createCommentModule
import com.github.diegoberaldin.raccoonforlemmy.unit.createpost.di.createPostModule
import com.github.diegoberaldin.raccoonforlemmy.unit.drafts.di.draftsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.di.drawerModule
import com.github.diegoberaldin.raccoonforlemmy.unit.editcommunity.di.editCommunityModule
import com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents.di.filteredContentsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.di.instanceInfoModule
import com.github.diegoberaldin.raccoonforlemmy.unit.licences.di.licenceModule
import com.github.diegoberaldin.raccoonforlemmy.unit.manageban.di.manageBanModule
import com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions.di.manageSubscriptionsModule
import com.github.diegoberaldin.raccoonforlemmy.unit.moderatewithreason.di.moderateWithReasonModule
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.di.modlogModule
import com.github.diegoberaldin.raccoonforlemmy.unit.postdetail.di.postDetailModule
import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.di.reportListModule
import com.github.diegoberaldin.raccoonforlemmy.unit.selectcommunity.di.selectCommunityModule
import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.di.selectInstanceModule
import com.github.diegoberaldin.raccoonforlemmy.unit.userdetail.di.userDetailModule
import com.github.diegoberaldin.raccoonforlemmy.unit.userinfo.di.userInfoModule
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.di.zoomableImageModule
import org.koin.core.context.startKoin
import platform.Foundation.NSBundle

fun initKoin() {
    startKoin {
        modules(
            internalSharedModule,
            coreAppearanceModule,
            corePreferencesModule,
            coreApiModule,
            coreIdentityModule,
            coreL10nModule,
            coreNotificationModule,
            corePersistenceModule,
            hapticFeedbackModule,
            shareHelperModule,
            galleryHelperModule,
            crashReportModule,
            repositoryModule,
            domainInboxModule,
            utilsModule,
            imagePreloadModule,
            networkModule,
            navigationModule,
            lemmyUiModule,
            homeTabModule,
            inboxTabModule,
            profileTabModule,
            searchTabModule,
            settingsTabModule,
            banModule,
            zoomableImageModule,
            chatModule,
            selectCommunityModule,
            drawerModule,
            communityInfoModule,
            instanceInfoModule,
            reportListModule,
            createPostModule,
            createCommentModule,
            postDetailModule,
            communityDetailModule,
            userDetailModule,
            userInfoModule,
            manageSubscriptionsModule,
            modlogModule,
            accountSettingsModule,
            manageBanModule,
            selectInstanceModule,
            configureSwipeActionsModule,
            configureContentViewModule,
            draftsModule,
            filteredContentsModule,
            editCommunityModule,
            licenceModule,
            appIconModule,
            fileSystemModule,
            coreResourceModule,
            paginationModule,
            customTabsModule,
            moderateWithReasonModule,
        )
    }

    AppInfo.versionCode =
        buildString {
            val dict = NSBundle.mainBundle.infoDictionary
            val buildNumber = dict?.get("CFBundleVersion") as? String ?: ""
            val versionName = dict?.get("CFBundleShortVersionString") as? String ?: ""
            if (versionName.isNotEmpty()) {
                append(versionName)
            }
            if (buildNumber.isNotEmpty()) {
                append(" (")
                append(buildNumber)
                append(")")
            }
        }
}
