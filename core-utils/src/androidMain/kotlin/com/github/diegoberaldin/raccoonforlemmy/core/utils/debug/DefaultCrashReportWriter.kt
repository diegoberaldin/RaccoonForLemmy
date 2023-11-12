package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter

class DefaultCrashReportWriter(
    private val context: Context,
) : CrashReportWriter {

    companion object {
        const val FILE_NAME = "crash_report.txt"
    }

    override fun write(reportText: String) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val state = Environment.getExternalStorageState()
        val logFile = if (Environment.MEDIA_MOUNTED == state) {
            File(dir, FILE_NAME)
        } else {
            File(context.cacheDir, FILE_NAME)
        }
        try {
            val writer = FileWriter(logFile, false)
            writer.append(reportText)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
