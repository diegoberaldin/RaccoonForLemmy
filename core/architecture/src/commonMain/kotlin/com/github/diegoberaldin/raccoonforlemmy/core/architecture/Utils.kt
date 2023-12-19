package com.github.diegoberaldin.raccoonforlemmy.core.architecture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun MviModel<*, *, *>.bindToLifecycle(key: Any = Unit) {
    DisposableEffect(key) {
        onStarted()
        onDispose(::onDisposed)
    }
}
