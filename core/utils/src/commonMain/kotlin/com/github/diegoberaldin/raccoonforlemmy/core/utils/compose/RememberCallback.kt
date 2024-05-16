package com.github.diegoberaldin.raccoonforlemmy.core.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberCallback(
    key: Any = Unit,
    block: () -> Unit,
): () -> Unit {
    return remember(key) {
        block
    }
}

@Composable
fun rememberCallback(
    key1: Any = Unit,
    key2: Any,
    block: () -> Unit,
): () -> Unit {
    return remember(key1, key2) {
        block
    }
}

@Composable
fun <T, U> rememberCallbackArgs(
    key: Any = Unit,
    block: (T) -> U,
): (T) -> U {
    return remember(key) {
        block
    }
}

@Composable
fun <T, U, V> rememberCallbackArgs(
    key: Any = Unit,
    block: (T, V) -> U,
): (T, V) -> U {
    return remember(key) {
        block
    }
}

@Composable
fun <T, U> rememberCallbackArgs(
    key1: Any = Unit,
    key2: Any,
    block: (T) -> U,
): (T) -> U {
    return remember(key1, key2) {
        block
    }
}
