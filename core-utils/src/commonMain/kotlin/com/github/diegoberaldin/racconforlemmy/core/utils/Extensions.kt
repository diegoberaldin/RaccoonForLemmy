package com.github.diegoberaldin.racconforlemmy.core.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

fun Modifier.onClick(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
    ) {
        onClick()
    }
}

@Composable
fun String.toLanguageName() = when (this) {
    "it" -> stringResource(MR.strings.language_it)
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
    val client = HttpClient(CIO)
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