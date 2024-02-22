package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.Android

actual fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return Android
}
