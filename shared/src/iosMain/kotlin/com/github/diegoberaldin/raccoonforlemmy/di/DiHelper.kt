package com.github.diegoberaldin.raccoonforlemmy.di

import com.github.diegoberaldin.racconforlemmy.core.utils.AppInfo
import com.github.diegoberaldin.racconforlemmy.core.utils.hapticFeedbackModule
import com.github.diegoberaldin.racconforlemmy.core.utils.shareHelperModule
import com.github.diegoberaldin.raccoonforlemmy.core.api.di.coreApiModule
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.coreAppearanceModule
import com.github.diegoberaldin.raccoonforlemmy.core.crashreport.di.crashReportModule
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.coreNotificationModule
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.di.corePreferencesModule
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.di.coreIdentityModule
import com.github.diegoberaldin.raccoonforlemmy.feature.home.di.homeTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di.inboxTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.profileTabModule
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.searchTabModule
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
            crashReportModule,
            hapticFeedbackModule,
            localizationModule,
            shareHelperModule,
            homeTabModule,
            inboxTabModule,
            profileTabModule,
            searchTabModule,
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
