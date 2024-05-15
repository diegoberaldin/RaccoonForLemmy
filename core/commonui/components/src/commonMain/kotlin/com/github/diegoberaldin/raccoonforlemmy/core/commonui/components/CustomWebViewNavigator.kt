package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal sealed interface WebViewNavigationEvent {
    data object GoBack : WebViewNavigationEvent
}

class WebViewNavigator(
    private val coroutineScope: CoroutineScope,
) {
    var canGoBack: Boolean = true
    internal val events = MutableSharedFlow<WebViewNavigationEvent>()

    fun goBack() {
        coroutineScope.launch {
            events.emit(WebViewNavigationEvent.GoBack)
        }
    }
}

@Composable
fun rememberWebViewNavigator(): WebViewNavigator {
    val scope = rememberCoroutineScope()
    return remember {
        WebViewNavigator(
            coroutineScope = scope,
        )
    }
}
