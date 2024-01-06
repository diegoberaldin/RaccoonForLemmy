package com.github.diegoberaldin.raccoonforlemmy.core.markdown.plugins

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.image.AsyncDrawableScheduler

data class SpoilerTitleSpan(val title: CharSequence)
class SpoilerCloseSpan

/*
 * Originally inspired by:
 * https://github.com/dessalines/jerboa/blob/main/app/src/main/java/com/jerboa/util/markwon/MarkwonSpoilerPlugin.kt
 */
class MarkwonSpoilerPlugin private constructor(
    private val enableInteraction: Boolean,
    private val onInteraction: () -> Unit,
) :
    AbstractMarkwonPlugin() {

    companion object {
        fun create(
            enableInteraction: Boolean = true,
            onInteraction: () -> Unit = {},
        ): MarkwonSpoilerPlugin {
            return MarkwonSpoilerPlugin(
                enableInteraction = enableInteraction,
                onInteraction = onInteraction,
            )
        }
    }

    override fun configure(registry: MarkwonPlugin.Registry) {
        registry.require(CorePlugin::class.java) {
            it.addOnTextAddedListener(
                SpoilerTextAddedListener(),
            )
        }
    }

    override fun afterSetText(textView: TextView) {
        runCatching {
            val spanned = SpannableStringBuilder(textView.text)
            val startSpans =
                spanned.getSpans(0, spanned.length, SpoilerTitleSpan::class.java)
                    .sortedBy { spanned.getSpanStart(it) }
            val closeSpans =
                spanned.getSpans(0, spanned.length, SpoilerCloseSpan::class.java)
                    .sortedBy { spanned.getSpanStart(it) }

            startSpans
                .zip(closeSpans)
                .forEach { (startSpan, closeSpan) ->
                    val spoilerStart = spanned.getSpanStart(startSpan)
                    val spoilerEnd = spanned.getSpanEnd(closeSpan)

                    val spoilerTitle = getSpoilerTitle(false, startSpan.title.toString())

                    val spoilerContent = spanned.subSequence(
                        spanned.getSpanEnd(startSpan) + 1,
                        spoilerEnd - 3,
                    )

                    // Remove spoiler content from span
                    spanned.replace(spoilerStart, spoilerEnd, spoilerTitle)

                    // Set span block title
                    spanned.setSpan(
                        spoilerTitle,
                        spoilerStart,
                        spoilerStart + spoilerTitle.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                    val wrapper = SpoilerClickableSpan(
                        enableInteraction = enableInteraction,
                        textView = textView,
                        spoilerTitle = startSpan.title.toString(),
                        spoilerContent = spoilerContent.toString(),
                        onInteraction = onInteraction,
                    )

                    // Set spoiler block type as ClickableSpan
                    spanned.setSpan(
                        wrapper,
                        spoilerStart,
                        spoilerStart + spoilerTitle.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )

                    textView.text = spanned
                }
        }
    }
}

private fun getSpoilerTitle(openParam: Boolean, title: String): String = if (openParam) {
    "▼ ${title}\n"
} else {
    // The space at the end is necessary for the lengths to be the same
    // This reduces complexity as else it would need complex logic to determine the replacement length
    "▶ ${title}\u200B"
}

private class SpoilerClickableSpan(
    private val enableInteraction: Boolean,
    private val textView: TextView,
    private val spoilerTitle: String,
    private val spoilerContent: String,
    private val onInteraction: () -> Unit,
) : ClickableSpan() {

    private var open = false

    override fun onClick(view: View) {
        if (enableInteraction) {
            onInteraction()
            textView.cancelPendingInputEvents()
            val spanned = SpannableStringBuilder(textView.text)
            val title = getSpoilerTitle(open, spoilerTitle)
            val start = spanned.indexOf(title).coerceAtLeast(0)

            open = !open

            spanned.replace(
                start,
                start + title.length,
                getSpoilerTitle(open, spoilerTitle),
            )
            if (open) {
                spanned.insert(start + title.length, spoilerContent)
            } else {
                spanned.replace(
                    start + title.length,
                    start + title.length + spoilerContent.length,
                    "",
                )
            }

            textView.text = spanned
            AsyncDrawableScheduler.schedule(textView)
        }
    }

    override fun updateDrawState(ds: TextPaint) {
    }
}

private class SpoilerTextAddedListener : CorePlugin.OnTextAddedListener {
    override fun onTextAdded(visitor: MarkwonVisitor, text: String, start: Int) {
        val spoilerTitleRegex = Regex("(:::\\s+spoiler\\s+)(.*)")
        // Find all spoiler "start" lines
        val spoilerTitles = spoilerTitleRegex.findAll(text)

        for (match in spoilerTitles) {
            val spoilerTitle = match.groups[2]!!.value
            visitor.builder().setSpan(
                SpoilerTitleSpan(spoilerTitle),
                start,
                start + match.groups[2]!!.range.last,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }

        val spoilerCloseRegex = Regex("^(?!.*spoiler).*:::")
        // Find all spoiler "end" lines
        val spoilerCloses = spoilerCloseRegex.findAll(text)
        for (match in spoilerCloses) {
            visitor.builder()
                .setSpan(SpoilerCloseSpan(), start, start + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}