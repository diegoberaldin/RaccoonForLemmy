package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.stringByAppendingPathComponent

class DefaultCrashReportWriter : CrashReportWriter {

    companion object {
        const val FILE_NAME = "crash_report.txt"
    }

    override fun write(reportText: String) {
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val file = (paths.first() as NSString).stringByAppendingPathComponent(FILE_NAME)
        val data = NSString.create(string = reportText).dataUsingEncoding(NSUTF8StringEncoding)
        NSFileManager.defaultManager.createFileAtPath(file, data, null)
    }
}
