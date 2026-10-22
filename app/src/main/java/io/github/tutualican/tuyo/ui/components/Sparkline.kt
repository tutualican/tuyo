package io.github.tutualican.tuyo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors

/**
 * A deliberately quiet chart: no axes, no labels, one accent line. The exact
 * numbers live next to it; the shape only has to answer "is this trending up?".
 */
@Composable
fun Sparkline(
    values: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = LocalTuyoColors.current.accent,
) {
    val colors = LocalTuyoColors.current
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val stroke = 1.6.dp.toPx()
        val horizontalPadding = 2.dp.toPx()
        val width = size.width - horizontalPadding * 2
        val height = size.height

        repeat(3) { index ->
            val y = height * (index + 1) / 4f
            drawLine(
                color = colors.hairline,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val step = if (values.size == 1) 0f else width / (values.size - 1)
        val points = values.mapIndexed { index, value ->
            val x = horizontalPadding + step * index
            val normalized = ((value - min) / range).toFloat()
            val y = height - normalized * (height - stroke * 2) - stroke
            Offset(x, y)
        }

        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fill = Path().apply {
            addPath(line)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.12f), Color.Transparent),
                startY = 0f,
                endY = height,
            ),
        )
        drawPath(
            path = line,
            color = color,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
        val last = points.last()
        drawCircle(color.copy(alpha = 0.16f), radius = 6.dp.toPx(), center = last)
        drawCircle(color, radius = 2.4.dp.toPx(), center = last)
    }
}
