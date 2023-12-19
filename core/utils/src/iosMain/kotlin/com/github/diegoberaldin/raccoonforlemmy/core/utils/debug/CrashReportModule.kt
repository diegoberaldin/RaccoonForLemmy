package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

actual val crashReportModule = module {
    single<CrashReportConfiguration> {
        DefaultCrashReportConfiguration()
    }
    single<CrashReportWriter> {
        DefaultCrashReportWriter()
    }
    single<CrashReportSender> {
        DefaultCrashReportSender()
    }
}

actual fun getCrashReportSender(): CrashReportSender = CrashReportDiHelper.crashReportSender
actual fun getCrashReportConfiguration(): CrashReportConfiguration =
    CrashReportDiHelper.crashReportConfiguration

internal object CrashReportDiHelper : KoinComponent {
    val crashReportSender: CrashReportSender by inject()
    val crashReportConfiguration: CrashReportConfiguration by inject()
}