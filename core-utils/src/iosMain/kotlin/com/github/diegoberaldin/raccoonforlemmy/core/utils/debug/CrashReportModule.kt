package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import org.koin.dsl.module

actual val crashReportModule = module {
    single<CrashReportConfiguration> {
        DefaultCrashReportConfiguration()
    }
    single<CrashReportWriter> {
        DefaultCrashReportWriter()
    }
}
