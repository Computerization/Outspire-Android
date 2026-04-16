package com.computerization.outspire.feature.cas

import android.graphics.Color as AndroidColor
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.computerization.outspire.BuildConfig
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

@Composable
internal fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    color: Color,
    maxLines: Int = Int.MAX_VALUE,
) {
    if (maxLines == Int.MAX_VALUE) {
        HtmlWebView(
            html = html,
            modifier = modifier,
            style = style,
            color = color,
        )
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    WRAP_CONTENT,
                )
                autoLinkMask = Linkify.WEB_URLS
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { view ->
            val normalizedHtml = sanitizeHtmlForPreview(decodePossiblyEscapedHtml(html))
            view.text = HtmlCompat.fromHtml(normalizedHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
            view.setTextColor(color.toArgb())
            view.textSize = style.fontSize.takeIf { it != TextStyle.Default.fontSize }?.value ?: 14.sp.value
            view.maxLines = maxLines
            view.ellipsize = if (maxLines == Int.MAX_VALUE) null else android.text.TextUtils.TruncateAt.END
        },
    )
}

@Composable
private fun HtmlWebView(
    html: String,
    modifier: Modifier,
    style: TextStyle,
    color: Color,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                setBackgroundColor(AndroidColor.TRANSPARENT)
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = false
            }
        },
        update = { view ->
            val content = sanitizeHtmlForWebView(decodePossiblyEscapedHtml(html), style, color)
            view.loadDataWithBaseURL(
                "${BuildConfig.TSIMS_BASE_URL}/",
                content,
                "text/html",
                "utf-8",
                null,
            )
        },
    )
}

private fun decodePossiblyEscapedHtml(input: String): String {
    var output = input.trim()
    repeat(3) {
        val prev = output
        output = output
            .replace("\\u003C", "<")
            .replace("\\u003E", ">")
            .replace("\\u0026", "&")
            .replace("\\/", "/")
            .replace("\\\"", "\"")
            .replace("\\r\\n", "\n")
            .replace("\\n", "\n")
            .replace("\\r", "")
        output = Parser.unescapeEntities(output, false)
        if (output.length >= 2 && output.first() == '"' && output.last() == '"') {
            output = output.substring(1, output.length - 1)
        }
        if (output == prev) return@repeat
    }
    return output
}

private fun sanitizeHtmlForPreview(input: String): String {
    val cleanedInput = input
        .replace(Regex("(?is)@font-face\\s*\\{.*?\\}"), "")
        .trim()
    val doc = Jsoup.parse(cleanedInput)
    doc.select("style,script,head,meta,link").remove()
    val bodyHtml = doc.body()?.html().orEmpty().trim()
    if (bodyHtml.isNotEmpty()) return bodyHtml
    return cleanedInput.replace(Regex("(?is)<style[^>]*>.*?</style>"), "").trim()
}

private fun sanitizeHtmlForWebView(input: String, style: TextStyle, color: Color): String {
    val source = input.ifBlank { "<p></p>" }
    val doc = if (source.contains("<html", ignoreCase = true)) {
        Jsoup.parse(source)
    } else {
        Jsoup.parse("<html><head></head><body>$source</body></html>")
    }
    doc.select("script").remove()
    val head = doc.head()
    if (head.selectFirst("meta[name=viewport]") == null) {
        head.appendElement("meta")
            .attr("name", "viewport")
            .attr("content", "width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no")
    }
    val fontPx = (style.fontSize.takeIf { it != TextStyle.Default.fontSize }?.value ?: 14f).toInt().coerceAtLeast(12)
    val colorHex = "#%06X".format(0xFFFFFF and color.toArgb())
    head.appendElement("style").appendText(
        """
        html, body { margin: 0; padding: 0; background: transparent; color: $colorHex; font-size: ${fontPx}px; line-height: 1.55; }
        img { max-width: 100%; height: auto; }
        table { max-width: 100%; }
        """.trimIndent()
    )
    return doc.outerHtml()
}
