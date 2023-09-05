package com.github.diegoberaldin.raccoonforlemmy.core.crashreport

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class DefaultCrashReportManager(
    private val context: Context,
) : CrashReportManager {
    override fun setup() {
        FirebaseApp.initializeApp(context)
    }

    override fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    override fun recordException(exc: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(exc)
    }
}