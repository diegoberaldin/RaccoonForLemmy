package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import org.koin.dsl.module

actual val crashReportModule = module {
    single<CrashReportConfiguration> {
        DefaultCrashReportConfiguration(
            context = get(),
        )
    }
    single<CrashReportWriter> {
        DefaultCrashReportWriter(
            context = get(),
        )
    }
}
