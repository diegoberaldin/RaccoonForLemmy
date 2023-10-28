package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose

import android.content.Context
import android.text.util.Linkify
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins.MarkwonSpoilerPlugin
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

class DefaultMarkwonProvider(
    context: Context,
    onOpenUrl: ((String) -> Unit)?,
    // TODO: handle image clicks in the future
    onOpenImage: ((String) -> Unit)?,
) : MarkwonProvider {
    override val markwon: Markwon

    init {
        markwon = Markwon.builder(context)
            .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(CoilImagesPlugin.create(context))
            .usePlugin(MarkwonSpoilerPlugin.create(true))
            .usePlugin(
                object : AbstractMarkwonPlugin() {
                    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                        builder.linkResolver { view, link ->
                            view.cancelPendingInputEvents()
                            onOpenUrl?.invoke(link)
                        }
                    }
                }
            )
            .build()
    }
}