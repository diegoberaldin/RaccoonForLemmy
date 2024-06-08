package com.github.diegoberaldin.raccoonforlemmy.unit.acknowledgements.datasource

import com.github.diegoberaldin.raccoonforlemmy.core.utils.network.provideHttpClientEngineFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

internal class DefaultAcknowledgementsRemoteDataSource(
    factory: HttpClientEngineFactory<*> = provideHttpClientEngineFactory(),
) : AcknowledgementsRemoteDataSource {
    private val client: HttpClient =
        HttpClient(factory) {
            install(HttpTimeout) {
                requestTimeoutMillis = 600_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }

    override suspend fun getAcknowledgements(): List<Acknowledgement>? =
        runCatching {
            client.request(JSON_URL).run {
                val text = bodyAsText()
                Json.decodeFromString<List<Acknowledgement>>(text)
            }
        }.getOrNull()

    companion object {
        private const val JSON_URL =
            "https://diegoberaldin.github.io/RaccoonForLemmy/acknowledgements.json"
    }
}
