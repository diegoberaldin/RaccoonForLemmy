package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import platform.Foundation.NSLog

actual fun logDebug(message: String) {
    NSLog(message)
}
