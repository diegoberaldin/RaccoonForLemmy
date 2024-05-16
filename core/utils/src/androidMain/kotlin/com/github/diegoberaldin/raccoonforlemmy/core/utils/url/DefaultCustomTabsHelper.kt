package com.github.diegoberaldin.raccoonforlemmy.core.utils.url

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import org.koin.java.KoinJavaComponent.inject

class DefaultCustomTabsHelper(
    private val context: Context,
) : CustomTabsHelper {
    private val packageName: String?
        get() = CustomTabsClient.getPackageName(context, emptyList())

    override val isSupported: Boolean by lazy {
        !packageName.isNullOrEmpty()
    }

    override fun handle(
        url: String,
        noHistory: Boolean,
    ) {
        val uri = Uri.parse(url)
        CustomTabsIntent.Builder()
            .apply {
                setShareState(CustomTabsIntent.SHARE_STATE_ON)
                setShowTitle(true)
            }
            .build()
            .run {
                intent.apply {
                    `package` = packageName
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    if (noHistory) {
                        flags = flags or Intent.FLAG_ACTIVITY_NO_HISTORY
                        putExtra(EXTRA_CHROME_INCOGNITO, true)
                    }
                }
                launchUrl(context, uri)
            }
    }

    companion object {
        private const val EXTRA_CHROME_INCOGNITO = "com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB"
    }
}

actual fun getCustomTabsHelper(): CustomTabsHelper {
    val res by inject<CustomTabsHelper>(CustomTabsHelper::class.java)
    return res
}
