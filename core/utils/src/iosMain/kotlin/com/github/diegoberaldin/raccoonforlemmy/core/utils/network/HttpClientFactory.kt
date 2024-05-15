package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*> {
    return CIO
}
