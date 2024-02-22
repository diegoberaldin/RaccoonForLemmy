package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import io.ktor.client.engine.HttpClientEngineFactory

expect fun provideHttpClientEngineFactory(): HttpClientEngineFactory<*>
