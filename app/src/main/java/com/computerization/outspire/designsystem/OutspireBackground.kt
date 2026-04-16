package com.computerization.outspire.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.material3.MaterialTheme

@Composable
fun OutspireBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val primary = cs.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cs.background),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackdrop(
                primary = primary,
                background = cs.background,
                dark = dark,
            )
        }
        content()
    }
}

private fun DrawScope.drawBackdrop(
    primary: Color,
    background: Color,
    dark: Boolean,
) {
    val w = size.width
    val h = size.height

    val a1 = if (dark) 0.14f else 0.10f
    val a2 = if (dark) 0.10f else 0.06f

    // Two large soft blobs, biased to the top, to keep the UI "airy" without looking busy.
    drawCircle(
        color = primary.copy(alpha = a1),
        radius = w * 0.85f,
        center = Offset(w * 0.10f, -h * 0.10f),
    )
    drawCircle(
        color = primary.copy(alpha = a2),
        radius = w * 0.95f,
        center = Offset(w * 1.15f, h * 0.10f),
    )

    // Fade to the base background to avoid tinting the content area too much.
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                background.copy(alpha = 0.65f),
                background,
            ),
            startY = 0f,
            endY = h,
        ),
        size = size,
    )
}
