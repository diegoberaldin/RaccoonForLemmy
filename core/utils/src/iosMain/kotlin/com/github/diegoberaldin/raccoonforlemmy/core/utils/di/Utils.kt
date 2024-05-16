package com.github.diegoberaldin.raccoonforlemmy.core.utils.di

import com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon.AppIconManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon.DefaultAppIconManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.CrashReportConfiguration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.CrashReportWriter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.DefaultCrashReportConfiguration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.DefaultCrashReportWriter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.fs.DefaultFileSystemManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.fs.FileSystemManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.DefaultGalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.GalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.DefaultImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.network.DefaultNetworkManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.network.NetworkManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.DefaultShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.share.ShareHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.CustomTabsHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.DefaultCustomTabsHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.DefaultHapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import org.koin.dsl.module

actual val imagePreloadModule =
    module {
        single<ImagePreloadManager> {
            DefaultImagePreloadManager()
        }
    }

actual val networkModule =
    module {
        single<NetworkManager> { DefaultNetworkManager() }
    }

actual val appIconModule =
    module {
        single<AppIconManager> {
            DefaultAppIconManager()
        }
    }

actual val crashReportModule =
    module {
        single<CrashReportConfiguration> {
            DefaultCrashReportConfiguration()
        }
        single<CrashReportWriter> {
            DefaultCrashReportWriter()
        }
    }

actual val fileSystemModule =
    module {
        single<FileSystemManager> {
            DefaultFileSystemManager()
        }
    }

actual val galleryHelperModule =
    module {
        single<GalleryHelper> {
            DefaultGalleryHelper()
        }
    }

actual val shareHelperModule =
    module {
        single<ShareHelper> {
            DefaultShareHelper()
        }
    }

actual val customTabsModule =
    module {
        single<CustomTabsHelper> {
            DefaultCustomTabsHelper()
        }
    }

actual val hapticFeedbackModule =
    module {
        single<HapticFeedback> {
            DefaultHapticFeedback()
        }
    }
