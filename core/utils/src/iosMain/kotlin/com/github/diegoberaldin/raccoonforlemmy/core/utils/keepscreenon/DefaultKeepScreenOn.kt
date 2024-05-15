package com.github.diegoberaldin.raccoonforlemmy.core.utils.keepscreenon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIApplication

class DefaultKeepScreenOn : KeepScreenOn {
    override fun activate() {
        UIApplication.sharedApplication().idleTimerDisabled = true
    }

    override fun deactivate() {
        UIApplication.sharedApplication().idleTimerDisabled = false
    }
}

@Composable
actual fun rememberKeepScreenOn(): KeepScreenOn {
    return remember {
        DefaultKeepScreenOn()
    }
}
