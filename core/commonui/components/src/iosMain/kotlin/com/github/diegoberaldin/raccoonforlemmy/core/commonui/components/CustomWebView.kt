package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.platform.LocalDensity
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIScrollView
import platform.UIKit.UIScrollViewDelegateProtocol
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CustomWebView(
    url: String,
    modifier: Modifier,
    navigator: WebViewNavigator,
    scrollConnection: NestedScrollConnection?,
) {
    var webView: WKWebView? = null

    LaunchedEffect(true) {
        navigator.events.onEach {
            when (it) {
                WebViewNavigationEvent.GoBack -> webView?.goBack()
            }
        }.launchIn(this)
    }

    val density = LocalDensity.current.density
    var lastOffsetX by remember { mutableStateOf(0f) }
    var lastOffsetY by remember { mutableStateOf(0f) }

    UIKitView(
        factory = {
            val config = WKWebViewConfiguration().apply {
                allowsInlineMediaPlayback = true
            }
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = config
            ).apply {
                userInteractionEnabled = true
                allowsBackForwardNavigationGestures = true
                val navigationDelegate = object : NSObject(), WKNavigationDelegateProtocol {
                    override fun webView(
                        webView: WKWebView,
                        didFinishNavigation: WKNavigation?,
                    ) {
                        navigator.canGoBack = webView.canGoBack
                    }
                }
                this.navigationDelegate = navigationDelegate
                this.scrollView.delegate = object : NSObject(), UIScrollViewDelegateProtocol {
                    override fun scrollViewDidScroll(scrollView: UIScrollView) {
                        scrollView.contentOffset.useContents {
                            val offsetX = (lastOffsetX - x).toFloat() / density
                            val offsetY = (lastOffsetY - y).toFloat() / density
                            scrollConnection?.onPreScroll(
                                available = Offset(offsetX, offsetY),
                                source = NestedScrollSource.Drag,
                            )
                            lastOffsetX = x.toFloat()
                            lastOffsetY = y.toFloat()
                        }
                    }
                }

            }.also {
                webView = it
            }
        },
        modifier = modifier,
        onRelease = {
            webView = null
        }
    )
}
