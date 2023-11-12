package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

import platform.Foundation.NSLog

actual object Log {
    actual fun d(message: String) {
        NSLog(message)
    }
}
