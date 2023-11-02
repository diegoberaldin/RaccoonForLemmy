package com.github.diegoberaldin.raccoonforlemmy.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
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
import kotlin.math.roundToInt

@Composable
fun String.toLanguageName() = when (this) {
    "de" -> stringResource(MR.strings.language_de)
    "el" -> stringResource(MR.strings.language_el)
    "es" -> stringResource(MR.strings.language_es)
    "fr" -> stringResource(MR.strings.language_fr)
    "it" -> stringResource(MR.strings.language_it)
    "pt" -> stringResource(MR.strings.language_pt)
    "ro" -> stringResource(MR.strings.language_ro)
    else -> stringResource(MR.strings.language_en)
}

@Composable
fun Dp.toLocalPixel(): Float = with(LocalDensity.current) {
    value * density
}

@Composable
fun Float.toLocalDp(): Dp = with(LocalDensity.current) {
    this@toLocalDp.toDp()
}

fun Int.getPrettyNumber(
    millionLabel: String,
    thousandLabel: String,
): String {
    return when {
        this > 1_000_000 -> (((this / 1_000_000.0) * 10).roundToInt() / 10.0).toString() + millionLabel
        this > 1_000 -> (((this / 1_000.0) * 10).roundToInt() / 10.0).toString() + thousandLabel
        else -> this.toString()
    }
}

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
