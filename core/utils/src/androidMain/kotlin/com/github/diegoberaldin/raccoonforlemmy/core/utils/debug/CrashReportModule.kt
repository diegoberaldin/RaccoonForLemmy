package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject

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
    single<CrashReportSender> {
        DefaultCrashReportSender(
            context = get(),
        )
    }
}

actual fun getCrashReportSender(): CrashReportSender {
    val res: CrashReportSender by inject(CrashReportSender::class.java)
    return res
}

actual fun getCrashReportConfiguration(): CrashReportConfiguration {
    val res: CrashReportConfiguration by inject(CrashReportConfiguration::class.java)
    return res
}

