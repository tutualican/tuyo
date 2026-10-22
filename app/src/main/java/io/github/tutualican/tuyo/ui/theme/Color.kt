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
)

val BrandBlue = Color(0xFF3B5BFE)
val BrandAmber = Color(0xFFFFC83D)

internal val DarkColors = TuyoColors(
    canvas = Color(0xFF08090A),
    surface = Color(0xFF0D0E10),
    raised = Color(0xFF14161A),
    overlay = Color(0xFF1A1C21),
    hairline = Color(0x14FFFFFF),
    hairlineStrong = Color(0x26FFFFFF),
    textPrimary = Color(0xFFF7F8F8),
    textSecondary = Color(0xFF8A8F98),
    textTertiary = Color(0xFF5C6169),
    accent = Color(0xFF6B7CFF),
    accentSoft = Color(0x1F6B7CFF),
    onAccent = Color(0xFFFFFFFF),
    success = Color(0xFF4CB782),
    warning = Color(0xFFF2C94C),
    danger = Color(0xFFEB5757),
    selection = Color(0xFF1B1E24),
)

internal val LightColors = TuyoColors(
    canvas = Color(0xFFFBFBFC),
    surface = Color(0xFFFFFFFF),
    raised = Color(0xFFF3F4F6),
    overlay = Color(0xFFFFFFFF),
    hairline = Color(0x14000000),
    hairlineStrong = Color(0x26000000),
    textPrimary = Color(0xFF151618),
    textSecondary = Color(0xFF5E6268),
    textTertiary = Color(0xFF8B8F96),
    accent = Color(0xFF3B5BFE),
    accentSoft = Color(0x143B5BFE),
    onAccent = Color(0xFFFFFFFF),
    success = Color(0xFF1F8A5B),
    warning = Color(0xFF8A6100),
    danger = Color(0xFFC23B3B),
    selection = Color(0xFFEFEFF2),
)
