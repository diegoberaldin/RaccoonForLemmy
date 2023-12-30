package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.VideoPlayer
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun PostCardVideo(
    modifier: Modifier = Modifier,
    url: String,
    blurred: Boolean = false,
    autoLoadImages: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    if (url.isNotEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1.33f)
                .onClick(),
            contentAlignment = Alignment.Center,
        ) {
            var shouldBeRendered by remember(autoLoadImages) { mutableStateOf(autoLoadImages) }
            var loading by remember { mutableStateOf(true) }
            if (shouldBeRendered) {
                VideoPlayer(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = if (blurred) 60.dp else 0.dp),
                    url = url,
                    onPlaybackStarted = {
                        loading = false
                    }
                )
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(backgroundColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(25.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        shouldBeRendered = true
                    },
                ) {
                    Text(
                        text = stringResource(MR.strings.button_load),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
