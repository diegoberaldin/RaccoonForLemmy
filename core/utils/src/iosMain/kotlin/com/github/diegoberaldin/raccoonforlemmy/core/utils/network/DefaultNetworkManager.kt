package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class DefaultNetworkManager : NetworkManager {
    override suspend fun isNetworkAvailable(): Boolean {
        val factory = provideHttpClientEngineFactory()
        val client = HttpClient(factory)
        return client.prepareGet("https://www.google.com").execute { httpResponse ->
            httpResponse.status == HttpStatusCode.OK
        }
    }
}
