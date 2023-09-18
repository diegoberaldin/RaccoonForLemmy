package com.github.diegoberaldin.racconforlemmy.core.utils

import org.koin.dsl.module
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

class DefaultShareHelper() : ShareHelper {
    override fun share(url: String, mimeType: String) {
        val shareActivity = UIActivityViewController(listOf(url), null)
        val rvc = UIApplication.sharedApplication().keyWindow?.rootViewController
        rvc?.presentViewController(shareActivity, true, null)
    }
}

actual val shareHelperModule = module {
    single<ShareHelper> {
        DefaultShareHelper()
    }
}
