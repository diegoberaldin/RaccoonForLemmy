package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeData

@OptIn(BetaInteropApi::class)
class DefaultCrashReportWriter : CrashReportWriter {
    companion object {
        const val FILE_NAME = "crash_report.txt"
    }

    override fun write(reportText: String) {
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val path = (paths.first() as NSString).stringByAppendingPathComponent(FILE_NAME)
        val data =
            NSString.create(string = reportText + "\n")
                .dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val existing = NSFileManager.defaultManager.fileExistsAtPath(path)
        if (!existing) {
            NSFileManager.defaultManager.createFileAtPath(path, data, null)
        } else {
            NSFileHandle.fileHandleForReadingAtPath(path)?.run {
                writeData(data)
                closeFile()
            }
        }
    }
}
