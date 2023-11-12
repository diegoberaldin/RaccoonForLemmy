package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return CIO
}