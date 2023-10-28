package com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins

import android.content.Context
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.image.ImageProps
import org.commonmark.node.Image


class ClickableImagesPlugin private constructor(
    private val context: Context,
    private val onOpenImage: (String) -> Unit,
) : AbstractMarkwonPlugin() {

    companion object {
        fun create(context: Context, onOpenImage: (String) -> Unit) =
            ClickableImagesPlugin(context, onOpenImage)
    }

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        val origin = builder.getFactory(Image::class.java)
        if (origin != null) {
            builder.setFactory(Image::class.java) { configuration, props ->
                val url = ImageProps.DESTINATION.require(props)
                val linkSpan = LinkSpan(
                    MarkwonTheme.create(context), url,
                ) { view, link ->
                    view.cancelPendingInputEvents()
                    onOpenImage(link)
                }
                arrayOf(
                    origin.getSpans(configuration, props),
                    linkSpan
                )
            }
        }
    }
}