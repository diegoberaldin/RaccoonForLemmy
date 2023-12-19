package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import android.content.Context
import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import com.google.firebase.FirebaseApp

class DefaultCrashReportSender(
    private val context: Context,
) : CrashReportSender {

    private var enabled = false

    override fun initialize() {
        FirebaseApp.initializeApp(context)
        enableCrashlytics()
    }

    override fun setEnabled(value: Boolean) {
        enabled = value
    }

    override fun sendFatalException(error: Throwable) {
        if (enabled) {
            CrashlyticsKotlin.sendFatalException(error)
        }
    }

    override fun sendNonFatalException(error: Throwable) {
        if (enabled) {
            CrashlyticsKotlin.sendHandledException(error)
        }
    }

    override fun log(message: String) {
        if (enabled) {
            CrashlyticsKotlin.logMessage(message)
        }
    }
}