package com.github.diegoberaldin.raccoonforlemmy.core.utils.di

import com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon.AppIconManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon.DefaultAppIconManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.DefaultImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.ImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.network.DefaultNetworkManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.network.NetworkManager
import org.koin.dsl.module

actual val imagePreloadModule = module {
    single<ImagePreloadManager> {
        DefaultImagePreloadManager()
    }
}

actual val networkModule = module {
    single<NetworkManager> { DefaultNetworkManager() }
}

actual val appIconModule = module {
    single<AppIconManager> {
        DefaultAppIconManager()
    }
}
