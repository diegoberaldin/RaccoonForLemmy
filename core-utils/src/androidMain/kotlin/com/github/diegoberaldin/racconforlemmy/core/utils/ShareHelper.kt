package com.github.diegoberaldin.racconforlemmy.core.utils

import android.content.Context
import android.content.Intent
import org.koin.dsl.module

class DefaultShareHelper(
    private val context: Context,
) : ShareHelper {
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
}

actual val shareHelperModule = module {
    single<ShareHelper> {
        DefaultShareHelper(
            context = get(),
        )
    }
}