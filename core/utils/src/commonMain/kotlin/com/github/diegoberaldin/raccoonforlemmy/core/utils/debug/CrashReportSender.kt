package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

interface CrashReportSender {
    fun initialize()
    fun setEnabled(value: Boolean)
    fun sendFatalException(error: Throwable)
    fun sendNonFatalException(error: Throwable)
    fun log(message: String)
}