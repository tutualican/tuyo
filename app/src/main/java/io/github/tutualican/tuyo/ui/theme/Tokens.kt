package io.github.tutualican.tuyo.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** A 4pt spacing grid; 20dp screen gutters read as Linear-dense on phones. */
object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 28.dp
    val xxxl = 40.dp
    val huge = 56.dp
}

object Radius {
    val sm = 6.dp
    val md = 10.dp
    val lg = 14.dp
    val xl = 18.dp
    val pill = 100.dp
}

val TuyoShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.sm),
    small = RoundedCornerShape(Radius.sm),
    medium = RoundedCornerShape(Radius.md),
    large = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(Radius.xl),
)

/**
 * Motion is quick and directional. Nothing bounces; the interface should feel
 * like it responded, not like it performed.
 */
object Motion {
    const val fast = 110
    const val base = 170
    const val slow = 240

    val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val enter: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val exit: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
}
