package com.github.diegoberaldin.raccoonforlemmy.core.utils

import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.stringWithFormat

actual fun String.isValidUrl(): Boolean = NSURL.URLWithString(this) != null

actual fun Int.toHexDigit(): String {
    return NSString.stringWithFormat("%02X", this)
}