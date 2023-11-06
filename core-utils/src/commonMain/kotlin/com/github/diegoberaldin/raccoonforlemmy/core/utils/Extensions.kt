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
import kotlin.math.round
import kotlin.time.Duration

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
    val value = this
    return when {
        value > 1_000_000 -> buildString {
            val rounded = round((value / 1_000_000.0) * 10) / 10
            if (rounded % 1 <= 0) {
                append(rounded.toInt())
            } else {
                append(rounded)
            }
            append(millionLabel)
        }

        value > 1_000 -> buildString {
            val rounded = round((value / 1_000.0) * 10) / 10
            if (rounded % 1 <= 0) {
                append(rounded.toInt())
            } else {
                append(rounded)
            }
            append(thousandLabel)
        }

        else -> buildString {
            append(value)
        }
    }
}

fun Duration.getPrettyDuration(
    secondsLabel: String,
    minutesLabel: String,
    hoursLabel: String,
): String = when {
    inWholeHours > 0 -> buildString {
        append(inWholeHours)
        append(hoursLabel)
        val remainderMinutes = inWholeMinutes % 60
        val remainderSeconds = inWholeSeconds % 60
        if (remainderMinutes > 0 || remainderSeconds > 0) {
            append(" ")
            append(remainderMinutes)
            append(minutesLabel)
        }
        if (remainderSeconds > 0) {
            append(" ")
            append(remainderSeconds)
            append(secondsLabel)
        }
    }

    inWholeMinutes > 0 -> buildString {
        append(inWholeMinutes)
        append(minutesLabel)
        val remainderSeconds = inWholeSeconds % 60
        if (remainderSeconds > 0) {
            append(" ")
            append(remainderSeconds)
            append(secondsLabel)
        }
    }

    else -> buildString {
        val rounded = round((inWholeMilliseconds / 1000.0) * 10.0) / 10.0
        if (rounded % 1 <= 0) {
            append(rounded.toInt())
        } else {
            append(rounded)
        }
        append(secondsLabel)
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
