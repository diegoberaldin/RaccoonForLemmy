package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

interface CrashReportWriter {
    fun write(reportText: String)
}