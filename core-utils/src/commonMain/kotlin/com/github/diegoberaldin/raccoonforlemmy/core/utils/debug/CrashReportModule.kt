package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import org.koin.core.module.Module

expect val crashReportModule: Module

expect fun getCrashReportSender(): CrashReportSender
expect fun getCrashReportConfiguration(): CrashReportConfiguration