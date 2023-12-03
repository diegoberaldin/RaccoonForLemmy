package com.github.diegoberaldin.raccoonforlemmy.core.markdown.provider

import android.content.Context
import android.text.util.Linkify
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins.ClickableImagesPlugin
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins.MarkwonLemmyLinkPlugin
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins.MarkwonSpoilerPlugin
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.gif.GifMediaDecoder
import io.noties.markwon.linkify.LinkifyPlugin

class DefaultMarkwonProvider(
    context: Context,
    onOpenUrl: ((String) -> Unit)?,
    onOpenImage: ((String) -> Unit)?,
    onImageTriggerUpdate: (() -> Unit)?,
) : MarkwonProvider {
    override val markwon: Markwon

    init {
        markwon = Markwon.builder(context)
            .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
            .usePlugin(MarkwonLemmyLinkPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(
                ImagesPlugin.create(
                    object : ImagesPlugin.ImagesConfigure {
                        override fun configureImages(plugin: ImagesPlugin) {
                            plugin.addMediaDecoder(GifMediaDecoder.create(true))
                        }
                    },
                ),
            )
            .usePlugin(MarkwonSpoilerPlugin.create(true))
            .usePlugin(
                ClickableImagesPlugin.create(
                    context = context,
                    onOpenImage = { url ->
                        onOpenImage?.invoke(url)
                    },
                    onTriggerUpdate = onImageTriggerUpdate ?: {},
                )
            ).usePlugin(
                object : AbstractMarkwonPlugin() {
                    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                        builder.linkResolver { view, link ->
                            view.cancelPendingInputEvents()
                            onOpenUrl?.invoke(link)
                        }
                    }
                },
            )
            .build()
    }
}