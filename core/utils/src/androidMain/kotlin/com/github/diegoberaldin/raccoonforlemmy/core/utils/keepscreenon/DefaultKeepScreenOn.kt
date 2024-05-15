package com.github.diegoberaldin.raccoonforlemmy.core.utils.keepscreenon

import android.app.Activity
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class DefaultKeepScreenOn(
    private val window: Window?,
) : KeepScreenOn {
    override fun activate() {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun deactivate() {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
internal inline fun <reified T> findFromContext(): T? {
    var context = LocalContext.current
    while (context !is T) {
        context = (context as? ContextWrapper)?.baseContext ?: return null
    }
    return context
}

@Composable
actual fun rememberKeepScreenOn(): KeepScreenOn {
    val activity = findFromContext<Activity>()
    return remember {
        DefaultKeepScreenOn(activity?.window)
    }
}
