package com.github.diegoberaldin.raccoonforlemmy.di

import com.github.diegoberaldin.raccoonforlemmy.core.api.di.coreApiModule
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.coreAppearanceModule
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.coreNotificationModule
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.corePersistenceModule
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.di.corePreferencesModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.AppInfo
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.crashReportModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.galleryHelperModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.shareHelperModule
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.hapticFeedbackModule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.di.coreIdentityModule
import com.github.diegoberaldin.raccoonforlemmy.feature.home.di.homeTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di.inboxTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.profileTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.exploreTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.di.settingsTabModule
import com.github.diegoberaldin.raccoonforlemmy.resources.di.localizationModule
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
            coreNotificationModule,
            corePersistenceModule,
            hapticFeedbackModule,
            localizationModule,
            shareHelperModule,
            galleryHelperModule,
            crashReportModule,
            homeTabModule,
            inboxTabModule,
            profileTabModule,
            exploreTabModule,
            settingsTabModule,
        )
    }

    AppInfo.versionCode = buildString {
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
