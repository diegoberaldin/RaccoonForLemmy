package com.github.diegoberaldin.raccoonforlemmy.core.markdown.provider

import android.content.Context
import android.text.Spanned
import android.text.util.Linkify
import android.widget.TextView
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins.ClickableImagesPlugin
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins.MarkwonLemmyLinkPlugin
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins.MarkwonSpoilerPlugin
import com.github.diegoberaldin.raccoonforlemmy.core.utils.imagepreload.getCoilImageLoader
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val OPEN_LINK_DELAY = 300L

class DefaultMarkwonProvider(
    context: Context,
    override var onOpenUrl: ((String) -> Unit)? = null,
    override var onOpenImage: ((String) -> Unit)? = null,
) : MarkwonProvider {

    override val markwon: Markwon
    override val blockClickPropagation = MutableStateFlow(false)
    private val scope = CoroutineScope(SupervisorJob())

    init {
        markwon = Markwon.builder(context)
            .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
            .usePlugin(MarkwonLemmyLinkPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(
                MarkwonSpoilerPlugin.create(
                    onInteraction =  {
                        blockClickPropagation.value = true
                        scope.launch {
                            delay(OPEN_LINK_DELAY)
                            blockClickPropagation.value = false
                        }
                    }
                )
            )
            .run {
                val imageLoader = getCoilImageLoader(context)
                usePlugin(CoilImagesPlugin.create(context, imageLoader))
            }
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun beforeSetText(textView: TextView, markdown: Spanned) {
                    AsyncDrawableScheduler.unschedule(textView)
                }

                override fun afterSetText(textView: TextView) {
                    AsyncDrawableScheduler.schedule(textView)
                }
            })
            .usePlugin(
                ClickableImagesPlugin.create(
                    context = context,
                    onOpenImage = { url ->
                        blockClickPropagation.value = true
                        onOpenImage?.invoke(url)
                        scope.launch {
                            delay(OPEN_LINK_DELAY)
                            blockClickPropagation.value = false
                        }
                    },
                )
            ).usePlugin(
                object : AbstractMarkwonPlugin() {
                    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                        builder.linkResolver { view, link ->
                            view.cancelPendingInputEvents()
                            blockClickPropagation.value = true
                            onOpenUrl?.invoke(link)
                            scope.launch {
                                delay(OPEN_LINK_DELAY)
                                blockClickPropagation.value = false
                            }
                        }
                    }
                },
            )
            .build()
    }
}