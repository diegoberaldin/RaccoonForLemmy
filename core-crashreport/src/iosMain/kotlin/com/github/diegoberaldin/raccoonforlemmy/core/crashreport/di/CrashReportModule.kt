package com.github.diegoberaldin.raccoonforlemmy.core.crashreport.di

import com.github.diegoberaldin.raccoonforlemmy.core.crashreport.CrashReportManager
import com.github.diegoberaldin.raccoonforlemmy.core.crashreport.DefaultCrashReportManager
import org.koin.dsl.module

actual val crashReportModule = module {
    single<CrashReportManager> {
        DefaultCrashReportManager()
    }
}