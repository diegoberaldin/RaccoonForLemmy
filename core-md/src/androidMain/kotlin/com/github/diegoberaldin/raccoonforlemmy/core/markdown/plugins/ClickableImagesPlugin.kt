package com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins

import android.content.Context
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.image.ImageProps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.commonmark.node.Image
import org.commonmark.node.Node


class ClickableImagesPlugin private constructor(
    private val context: Context,
    private val onOpenImage: (String) -> Unit,
    private val onTriggerUpdate: () -> Unit,
) : AbstractMarkwonPlugin() {

    private val scope = CoroutineScope(SupervisorJob())

    companion object {
        fun create(context: Context, onOpenImage: (String) -> Unit, onTriggerUpdate: () -> Unit) =
            ClickableImagesPlugin(context, onOpenImage, onTriggerUpdate)
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

    override fun afterRender(node: Node, visitor: MarkwonVisitor) {
        super.afterRender(node, visitor)
        scope.launch {
            // trigger a recomposition to have image adapting to final size
            delay(150)
            onTriggerUpdate()
        }
    }
}