package com.github.diegoberaldin.raccoonforlemmy.core.utils

import android.util.Patterns

actual fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

actual fun Int.toHexDigit(): String = String.format("%02X", this)
