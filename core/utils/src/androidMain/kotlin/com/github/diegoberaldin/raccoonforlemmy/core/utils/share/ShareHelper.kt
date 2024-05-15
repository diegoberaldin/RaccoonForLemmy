package com.github.diegoberaldin.raccoonforlemmy.core.utils.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.koin.java.KoinJavaComponent.inject

class DefaultShareHelper(
    private val context: Context,
) : ShareHelper {

    override val supportsShareImage = true

    override fun share(url: String, mimeType: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = mimeType
        }

        val shareIntent = Intent.createChooser(sendIntent, null).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(shareIntent)
    }

    override fun shareImage(path: Any?, mimeType: String) {
        val uri = path as? Uri ?: throw Exception("Path must be an Uri")
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
        }

        val shareIntent = Intent.createChooser(sendIntent, null).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(shareIntent)
    }
}

actual fun getShareHelper(): ShareHelper {
    val res: ShareHelper by inject(ShareHelper::class.java)
    return res
}
