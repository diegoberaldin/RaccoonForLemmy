package com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins


import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import android.text.util.Linkify
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.SpannableBuilder
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.CoreProps
import org.commonmark.node.Link
import java.util.regex.Pattern


private const val COMMUNITY_FRAGMENT: String = """[a-zA-Z0-9_]{3,}"""


private const val INSTANCE_FRAGMENT: String =
    """([a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]\.)+[a-zA-Z]{2,}"""

private const val USER_FRAGMENT: String = """[a-zA-Z0-9_]{3,}"""

private val lemmyCommunityPattern: Pattern =
    Pattern.compile("(?<!\\S)!($COMMUNITY_FRAGMENT)(?:@($INSTANCE_FRAGMENT))?\\b")


private val lemmyUserPattern: Pattern =
    Pattern.compile("(?<!\\S)@($USER_FRAGMENT)(?:@($INSTANCE_FRAGMENT))?\\b")

class MarkwonLemmyLinkPlugin private constructor() : AbstractMarkwonPlugin() {

    companion object {
        fun create() = MarkwonLemmyLinkPlugin()
    }

    override fun configure(registry: MarkwonPlugin.Registry) {
        registry.require(CorePlugin::class.java) { it.addOnTextAddedListener(LemmyTextAddedListener()) }
    }

    private class LemmyTextAddedListener : CorePlugin.OnTextAddedListener {
        override fun onTextAdded(visitor: MarkwonVisitor, text: String, start: Int) {
            val spanFactory = visitor.configuration().spansFactory().get(
                Link::class.java,
            ) ?: return

            val builder = SpannableStringBuilder(text)
            if (addLinks(builder)) {
                // target URL span specifically
                val spans = builder.getSpans(0, builder.length, URLSpan::class.java)
                if (!spans.isNullOrEmpty()) {
                    val renderProps = visitor.renderProps()
                    val spannableBuilder = visitor.builder()
                    for (span in spans) {
                        CoreProps.LINK_DESTINATION[renderProps] = span.url
                        SpannableBuilder.setSpans(
                            spannableBuilder,
                            spanFactory.getSpans(visitor.configuration(), renderProps),
                            start + builder.getSpanStart(span),
                            start + builder.getSpanEnd(span),
                        )
                    }
                }
            }
        }

        fun addLinks(text: Spannable): Boolean {
            val communityLinkAdded = Linkify.addLinks(text, lemmyCommunityPattern, null)
            val userLinkAdded = Linkify.addLinks(text, lemmyUserPattern, null)
            return communityLinkAdded || userLinkAdded
        }
    }
}