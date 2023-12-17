package com.github.diegoberaldin.raccoonforlemmy.core.utils.di

import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.DefaultImagePreloadManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.ImagePreloadManager
import org.koin.dsl.module

actual val imagePreloadModule = module {
    single<ImagePreloadManager> {
        DefaultImagePreloadManager(
            context = get(),
        )
    }
}