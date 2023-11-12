package com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery

import com.github.diegoberaldin.raccoonforlemmy.core.utils.network.provideHttpClientEngineFactory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

suspend fun GalleryHelper.download(url: String): ByteArray = withContext(Dispatchers.IO) {
    val factory = provideHttpClientEngineFactory()
    val client = HttpClient(factory)
    client.prepareGet(url).execute { httpResponse ->
        val channel: ByteReadChannel = httpResponse.body()
        var result = byteArrayOf()
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(4096)
            while (!packet.isEmpty) {
                val bytes = packet.readBytes()
                result += bytes
                println("Received ${result.size} bytes / ${httpResponse.contentLength()}")
            }
        }
        result
    }
}
