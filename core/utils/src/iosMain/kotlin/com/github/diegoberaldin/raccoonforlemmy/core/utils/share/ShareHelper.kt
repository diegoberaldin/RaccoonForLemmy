package com.github.diegoberaldin.raccoonforlemmy.core.utils.share

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage

class DefaultShareHelper : ShareHelper {
    override val supportsShareImage = false

    override fun share(url: String, mimeType: String) {
        val shareActivity = UIActivityViewController(listOf(url), null)
        val rvc = UIApplication.sharedApplication().keyWindow?.rootViewController
        rvc?.presentViewController(shareActivity, true, null)
    }

    override fun shareImage(path: Any?, mimeType: String) {
        val shareActivity = UIActivityViewController(listOf(UIImage(contentsOfFile = path?.toString())), null)
        val rvc = UIApplication.sharedApplication().keyWindow?.rootViewController
        rvc?.presentViewController(shareActivity, true, null)
    }
}

actual val shareHelperModule = module {
    single<ShareHelper> {
        DefaultShareHelper()
    }
}

actual fun getShareHelper(): ShareHelper = ShareHelperInjectHelper.shareHelper

private object ShareHelperInjectHelper : KoinComponent {
    val shareHelper: ShareHelper by inject()
}