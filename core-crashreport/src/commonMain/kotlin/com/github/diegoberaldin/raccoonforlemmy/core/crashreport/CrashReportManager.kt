package com.github.diegoberaldin.raccoonforlemmy.core.crashreport

interface CrashReportManager {
    fun setup()
    fun log(message: String)
    fun recordException(exc: Throwable)
}
