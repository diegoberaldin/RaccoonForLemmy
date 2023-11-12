package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

interface CrashReportConfiguration {

    companion object {
        const val PREFERENCES_NAME = "CrashReportPreferences"
    }

    fun isEnabled(): Boolean

    fun setEnabled(value: Boolean)
}
