package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import android.media.session.PlaybackState
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    onPlaybackStarted: (() -> Unit)?,
) {
    val context = LocalContext.current
    val exoPlayer =
        remember {
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    val defaultDataSourceFactory = DefaultDataSource.Factory(context)
                    val dataSourceFactory: DataSource.Factory =
                        DefaultDataSource.Factory(
                            context,
                            defaultDataSourceFactory,
                        )
                    val source =
                        ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(url))
                    setMediaSource(source)

                    addListener(
                        object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                super.onPlaybackStateChanged(playbackState)
                                if (playbackState == PlaybackState.STATE_PLAYING) {
                                    onPlaybackStarted?.invoke()
                                }
                            }
                        },
                    )
                    prepare()
                }.apply {
                    playWhenReady = true
                    videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = 0f
                }
        }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(context).apply {
                controllerAutoShow = true
                controllerHideOnTouch = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
    )

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}
