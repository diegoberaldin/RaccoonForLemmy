package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import android.content.Context
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.CrashReportConfiguration.Companion.PREFERENCES_NAME

class DefaultCrashReportConfiguration(
    private val context: Context,
) : CrashReportConfiguration {
    companion object {
        const val KEY = "crashReportEnabled"
    }

    override fun isEnabled(): Boolean =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY, false)

    override fun setEnabled(value: Boolean) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).apply {
            edit().putBoolean(KEY, value).apply()
        }
    }
}
