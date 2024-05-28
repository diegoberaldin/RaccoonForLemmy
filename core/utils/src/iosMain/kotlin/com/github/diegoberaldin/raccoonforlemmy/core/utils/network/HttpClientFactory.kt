package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return Darwin
}
