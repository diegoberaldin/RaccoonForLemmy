package com.github.diegoberaldin.raccoonforlemmy.core.utils.debug

private const val TAG = "com.github.diegoberaldin.raccoonforlemmy"
actual fun logDebug(message: String) {
    android.util.Log.d(TAG, message)
}
