package io.github.tutualican.tuyo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Semantic colour tokens. Screens never use raw hex values; every colour has a
 * job, and the light palette is a first-class citizen rather than an inversion.
 */
@Immutable
data class TuyoColors(
    val canvas: Color,
    val surface: Color,
    val raised: Color,
    val overlay: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val selection: Color,
    /** Pale card washes. Used sparingly to give a screen a warm, human rhythm. */
    val warmWash: Color,
    val coolWash: Color,
    val freshWash: Color,
)

val BrandBlue = Color(0xFF3B5BFE)
val BrandAmber = Color(0xFFFFC83D)

internal val DarkColors = TuyoColors(
    canvas = Color(0xFF111417),
    surface = Color(0xFF181C21),
    raised = Color(0xFF22272E),
    overlay = Color(0xFF1D2228),
    hairline = Color(0x1AFFFFFF),
    hairlineStrong = Color(0x2EFFFFFF),
    textPrimary = Color(0xFFF5F0E9),
    textSecondary = Color(0xFFB3AAA0),
    textTertiary = Color(0xFF91887D),
    accent = Color(0xFF8EA2FF),
    accentSoft = Color(0x262F3E6E),
    onAccent = Color(0xFF101426),
    success = Color(0xFF6BC9A0),
    warning = Color(0xFFF0C16B),
    danger = Color(0xFFF08C8C),
    selection = Color(0xFF252B33),
    warmWash = Color(0xFF2B241D),
    coolWash = Color(0xFF1E2838),
    freshWash = Color(0xFF1C2B25),
)

internal val LightColors = TuyoColors(
    canvas = Color(0xFFFAF7F2),
    surface = Color(0xFFFFFDFA),
    raised = Color(0xFFF4EFE7),
    overlay = Color(0xFFFFFDFA),
    hairline = Color(0x14000000),
    hairlineStrong = Color(0x26000000),
    textPrimary = Color(0xFF26221D),
    textSecondary = Color(0xFF6E6458),
    textTertiary = Color(0xFF786E61),
    accent = Color(0xFF3F63D8),
    accentSoft = Color(0xFFE9EDFC),
    onAccent = Color(0xFFFFFFFF),
    success = Color(0xFF1F714D),
    warning = Color(0xFF985A0B),
    danger = Color(0xFFC25151),
    selection = Color(0xFFF2EADF),
    warmWash = Color(0xFFFFF0E1),
    coolWash = Color(0xFFECF1FD),
    freshWash = Color(0xFFEAF5EE),
)
