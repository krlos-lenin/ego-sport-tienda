package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SneakerVisual(
    primaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showContainer: Boolean = true
) {
    val containerModifier = if (showContainer) {
        modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF27272A),
                        Color(0xFF18181B)
                    )
                )
            )
            .padding(8.dp)
    } else {
        modifier.size(size)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Scale coordinates based on canvas size
            // Sneaker silhouette coordinates in normalized 0.0 - 1.0 range
            fun px(nx: Float) = nx * w
            fun py(ny: Float) = ny * h

            // 1. Subtle shadow under sole
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66000000), Color.Transparent),
                    center = Offset(px(0.5f), py(0.85f)),
                    radius = px(0.42f)
                ),
                topLeft = Offset(px(0.12f), py(0.78f)),
                size = androidx.compose.ui.geometry.Size(px(0.76f), py(0.14f))
            )

            // 2. Sneaker Midsole & Outsole (Rubber base)
            val solePath = Path().apply {
                moveTo(px(0.15f), py(0.70f))
                lineTo(px(0.82f), py(0.70f))
                // Toe curve
                cubicTo(px(0.86f), py(0.70f), px(0.88f), py(0.73f), px(0.86f), py(0.78f))
                // Bottom curve back to heel
                lineTo(px(0.18f), py(0.78f))
                // Heel curve
                cubicTo(px(0.14f), py(0.78f), px(0.13f), py(0.74f), px(0.15f), py(0.70f))
                close()
            }
            drawPath(solePath, color = Color(0xFFF4F4F5), style = Fill)
            drawPath(solePath, color = Color(0xFFE4E4E7), style = Stroke(width = px(0.015f)))

            // Outsole bottom grip line
            val gripPath = Path().apply {
                moveTo(px(0.18f), py(0.77f))
                lineTo(px(0.85f), py(0.77f))
            }
            drawPath(
                gripPath,
                color = accentColor.copy(alpha = 0.85f),
                style = Stroke(width = px(0.025f), cap = StrokeCap.Round)
            )

            // 3. Sneaker Main Body (Upper)
            val upperPath = Path().apply {
                moveTo(px(0.17f), py(0.70f)) // heel bottom
                lineTo(px(0.18f), py(0.44f)) // heel top
                // Collar curve
                cubicTo(px(0.20f), py(0.40f), px(0.26f), py(0.38f), px(0.34f), py(0.40f))
                // Tongue slope down towards vamp
                lineTo(px(0.48f), py(0.48f))
                // Eyestay to forefoot
                lineTo(px(0.66f), py(0.56f))
                // Toe box curve down to sole
                cubicTo(px(0.76f), py(0.60f), px(0.84f), py(0.64f), px(0.82f), py(0.70f))
                close()
            }
            drawPath(upperPath, color = primaryColor, style = Fill)

            // 4. Heel counter overlay / Collar cushion
            val collarPath = Path().apply {
                moveTo(px(0.18f), py(0.44f))
                cubicTo(px(0.22f), py(0.39f), px(0.28f), py(0.38f), px(0.34f), py(0.40f))
                lineTo(px(0.31f), py(0.46f))
                cubicTo(px(0.26f), py(0.44f), px(0.22f), py(0.45f), px(0.18f), py(0.48f))
                close()
            }
            drawPath(collarPath, color = Color(0xFF1E293B), style = Fill)

            // 5. Dynamic Flash / Stripe / Swoosh accent
            val accentStripe = Path().apply {
                moveTo(px(0.28f), py(0.64f))
                cubicTo(px(0.42f), py(0.63f), px(0.56f), py(0.58f), px(0.72f), py(0.50f))
                cubicTo(px(0.70f), py(0.54f), px(0.56f), py(0.64f), px(0.34f), py(0.67f))
                close()
            }
            drawPath(accentStripe, color = accentColor, style = Fill)
            drawPath(accentStripe, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = px(0.01f)))

            // 6. Laces / Eyelets details
            val laceColor = Color.White.copy(alpha = 0.9f)
            val laceStroke = Stroke(width = px(0.02f), cap = StrokeCap.Round)
            drawLine(
                color = laceColor,
                start = Offset(px(0.36f), py(0.44f)),
                end = Offset(px(0.42f), py(0.42f)),
                strokeWidth = px(0.022f),
                cap = StrokeCap.Round
            )
            drawLine(
                color = laceColor,
                start = Offset(px(0.43f), py(0.48f)),
                end = Offset(px(0.49f), py(0.46f)),
                strokeWidth = px(0.022f),
                cap = StrokeCap.Round
            )
            drawLine(
                color = laceColor,
                start = Offset(px(0.50f), py(0.52f)),
                end = Offset(px(0.56f), py(0.50f)),
                strokeWidth = px(0.022f),
                cap = StrokeCap.Round
            )

            // 7. Toe tip cap accent
            val toeCap = Path().apply {
                moveTo(px(0.72f), py(0.62f))
                cubicTo(px(0.77f), py(0.64f), px(0.83f), py(0.67f), px(0.82f), py(0.70f))
                lineTo(px(0.69f), py(0.70f))
                close()
            }
            drawPath(toeCap, color = Color.Black.copy(alpha = 0.25f), style = Fill)
        }
    }
}
