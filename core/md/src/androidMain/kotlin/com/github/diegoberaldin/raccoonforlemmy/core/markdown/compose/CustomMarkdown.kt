package com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.MarkdownColors
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.MarkdownPadding
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.MarkdownTypography
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.compose.model.ReferenceLinkHandlerImpl
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.di.getMarkwonProvider
import com.github.diegoberaldin.raccoonforlemmy.core.markdown.provider.MarkwonProvider
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.epochMillis
import io.noties.markwon.image.AsyncDrawableSpan
import kotlinx.coroutines.delay

/*
 * CREDITS:
 * https://github.com/dessalines/jerboa/blob/main/app/src/main/java/com/jerboa/ui/components/common/MarkdownHelper.kt
 */
@Composable
actual fun CustomMarkdown(
    content: String,
    modifier: Modifier,
    colors: MarkdownColors,
    typography: MarkdownTypography,
    padding: MarkdownPadding,
    maxLines: Int?,
    onOpenUrl: ((String) -> Unit)?,
    inlineImages: Boolean,
    autoLoadImages: Boolean,
    onOpenImage: ((String) -> Unit)?,
    onClick: (() -> Unit)?,
    onDoubleClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    CompositionLocalProvider(
        LocalReferenceLinkHandler provides ReferenceLinkHandlerImpl(),
        LocalMarkdownPadding provides padding,
        LocalMarkdownColors provides colors,
        LocalMarkdownTypography provides typography,
    ) {
        var imageRecompositionTrigger by remember { mutableStateOf(false) }
        var hasImages by remember { mutableStateOf(false) }
        val markwonProvider: MarkwonProvider = remember { getMarkwonProvider() }
        markwonProvider.onOpenUrl = onOpenUrl
        markwonProvider.onOpenImage = onOpenImage

        BoxWithConstraints(
            modifier = modifier
        ) {
            val style = LocalMarkdownTypography.current.text
            val fontScale = LocalDensity.current.fontScale * 1.3f
            val canvasWidthMaybe = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
            val textSizeMaybe = with(LocalDensity.current) { (style.fontSize * fontScale).toPx() }
            val defaultColor = LocalMarkdownColors.current.text
            val resolver: FontFamily.Resolver = LocalFontFamilyResolver.current
            val typeface: Typeface = remember(resolver, style) {
                resolver.resolve(
                    fontFamily = style.fontFamily,
                    fontWeight = style.fontWeight ?: FontWeight.Normal,
                    fontStyle = style.fontStyle ?: FontStyle.Normal,
                    fontSynthesis = style.fontSynthesis ?: FontSynthesis.All,
                )
            }.value as Typeface

            key(imageRecompositionTrigger) {
                AndroidView(
                    factory = { ctx ->
                        createTextView(
                            context = ctx,
                            textColor = defaultColor,
                            style = style,
                            typeface = typeface,
                            maxLines = maxLines,
                            fontSize = style.fontSize * fontScale,
                        ).apply {
                            val gestureDetector =
                                GestureDetector(
                                    ctx,
                                    object : GestureDetector.SimpleOnGestureListener() {

                                        private var lastClickTime = 0L

                                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                                            val currentTime = epochMillis()
                                            if ((currentTime - lastClickTime) < 300) return false
                                            lastClickTime = currentTime
                                            if (!markwonProvider.blockClickPropagation.value) {
                                                cancelPendingInputEvents()
                                                onClick?.invoke()
                                            }
                                            return true
                                        }

                                        override fun onDoubleTap(e: MotionEvent): Boolean {
                                            val currentTime = epochMillis()
                                            if ((currentTime - lastClickTime) < 300) return false
                                            lastClickTime = currentTime
                                            if (!markwonProvider.blockClickPropagation.value) {
                                                cancelPendingInputEvents()
                                                onDoubleClick?.invoke()
                                            }
                                            return true
                                        }

                                        override fun onLongPress(e: MotionEvent) {
                                            if (!markwonProvider.blockClickPropagation.value) {
                                                cancelPendingInputEvents()
                                                onLongClick?.invoke()
                                            }
                                        }

                                        override fun onDown(e: MotionEvent): Boolean {
                                            return true
                                        }
                                    }
                                )
                            setOnTouchListener { v, evt ->
                                if (evt.action == MotionEvent.ACTION_UP) {
                                    v.performClick()
                                }
                                gestureDetector.onTouchEvent(evt)
                            }
                        }
                    },
                    update = { textView ->
                        val md = markwonProvider.markwon.toMarkdown(content)
                        val imageSpans = md.getSpans(0, md.length, AsyncDrawableSpan::class.java)
                        for (img in imageSpans) {
                            img.drawable.initWithKnownDimensions(canvasWidthMaybe, textSizeMaybe)
                        }
                        markwonProvider.markwon.setParsedMarkdown(textView, md)
                        if (imageSpans.isNotEmpty()) {
                            hasImages = true
                        }
                    },
                )
            }

            LaunchedEffect(hasImages) {
                if (hasImages && !imageRecompositionTrigger) {
                    delay(500)
                    imageRecompositionTrigger = true
                }
            }
        }
    }
}

private fun createTextView(
    context: Context,
    textColor: Color,
    maxLines: Int? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    typeface: Typeface? = null,
    style: TextStyle,
    @IdRes viewId: Int? = null,
): TextView {
    val mergedStyle = style.merge(
        TextStyle(
            color = textColor,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
            textAlign = textAlign,
        ),
    )
    return TextView(context).apply {
        setTextColor(textColor.toArgb())
        setTextSize(TypedValue.COMPLEX_UNIT_SP, mergedStyle.fontSize.value)
        width = maxWidth
        if (maxLines != null) {
            this.maxLines = maxLines
        }

        viewId?.let { id = viewId }
        textAlign?.let { align ->
            textAlignment = when (align) {
                TextAlign.Left, TextAlign.Start -> View.TEXT_ALIGNMENT_TEXT_START
                TextAlign.Right, TextAlign.End -> View.TEXT_ALIGNMENT_TEXT_END
                TextAlign.Center -> View.TEXT_ALIGNMENT_CENTER
                else -> View.TEXT_ALIGNMENT_TEXT_START
            }
        }

        this.typeface = typeface
    }
}
