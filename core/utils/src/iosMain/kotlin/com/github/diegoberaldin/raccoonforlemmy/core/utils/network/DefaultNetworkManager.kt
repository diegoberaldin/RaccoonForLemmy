package com.github.diegoberaldin.raccoonforlemmy.core.utils.network

import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import io.ktor.http.HttpStatusCode

class DefaultNetworkManager : NetworkManager {

    override suspend fun isNetworkAvailable(): Boolean {
        val factory = provideHttpClientEngineFactory()
        val client = HttpClient(factory)
        return client.prepareGet("https://www.google.com").execute { httpResponse ->
            httpResponse.status == HttpStatusCode.OK
        }
    }
}