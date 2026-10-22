@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package io.github.tutualican.tuyo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.tutualican.tuyo.R

/**
 * Inter is bundled as one variable file. Declaring the real axes (instead of
 * letting the platform fake bold) keeps headings crisp on every density.
 */
private fun inter(weight: Int): Font = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val TuyoFontFamily = FontFamily(
    inter(400),
    inter(500),
    inter(600),
    inter(700),
)

private val Display = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 38.sp,
    lineHeight = 42.sp,
    letterSpacing = (-1.4).sp,
)

private val DisplaySmall = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 30.sp,
    lineHeight = 34.sp,
    letterSpacing = (-1.0).sp,
)

private val Headline = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 24.sp,
    lineHeight = 29.sp,
    letterSpacing = (-0.5).sp,
)

private val HeadlineSmall = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 20.sp,
    lineHeight = 25.sp,
    letterSpacing = (-0.3).sp,
)

private val Title = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.2).sp,
)

private val TitleSmall = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W500,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.1).sp,
)

private val Body = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W400,
    fontSize = 15.sp,
    lineHeight = 23.sp,
    letterSpacing = 0.sp,
)

private val BodySmall = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W400,
    fontSize = 14.sp,
    lineHeight = 21.sp,
    letterSpacing = 0.sp,
)

private val Caption = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W400,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
)

private val Label = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W500,
    fontSize = 13.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
)

private val Micro = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W500,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.2.sp,
)

/** Tabular figures for nets, counts and dates. */
val NumericStyle = TextStyle(
    fontFamily = TuyoFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 42.sp,
    lineHeight = 46.sp,
    letterSpacing = (-1.6).sp,
    fontFeatureSettings = "tnum",
)

val TuyoTypography = Typography(
    displayLarge = Display,
    displayMedium = DisplaySmall,
    displaySmall = Headline,
    headlineLarge = Headline,
    headlineMedium = HeadlineSmall,
    headlineSmall = Title,
    titleLarge = Title,
    titleMedium = TitleSmall,
    titleSmall = Label,
    bodyLarge = Body,
    bodyMedium = BodySmall,
    bodySmall = Caption,
    labelLarge = Label,
    labelMedium = Micro,
    labelSmall = Micro,
)
