package com.github.diegoberaldin.raccoonforlemmy.core.crashreport.di

import android.content.Context
import com.github.diegoberaldin.raccoonforlemmy.core.crashreport.CrashReportManager
import com.github.diegoberaldin.raccoonforlemmy.core.crashreport.DefaultCrashReportManager
import org.koin.dsl.module

actual val crashReportModule = module {
    single<CrashReportManager> {
        val context: Context by inject()
        DefaultCrashReportManager(context)
    }
}