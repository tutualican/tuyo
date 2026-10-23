package io.github.tutualican.tuyo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.EvidenceLevel
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Motion
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import io.github.tutualican.tuyo.ui.theme.TuyoFontFamily

@Composable
fun TuyoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(Space.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalTuyoColors.current
    val shape = RoundedCornerShape(Radius.lg)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.993f else 1f,
        animationSpec = tween(Motion.fast, easing = Motion.standard),
        label = "cardPress",
    )
    var m = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .background(colors.surface)
        .border(
            width = 1.dp,
            color = if (highlighted) colors.accent.copy(alpha = 0.32f) else colors.hairline,
            shape = shape,
        )
    if (onClick != null) {
        m = m.clickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
    }
    Column(modifier = m.padding(contentPadding), content = content)
}

@Composable
fun TuyoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TuyoButtonVariant = TuyoButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = LocalTuyoColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = tween(Motion.fast, easing = Motion.standard),
        label = "buttonPress",
    )
    val shape = RoundedCornerShape(Radius.sm)
    val background = when (variant) {
        TuyoButtonVariant.Primary -> colors.accent
        TuyoButtonVariant.Secondary -> Color.Transparent
        TuyoButtonVariant.Ghost -> Color.Transparent
        TuyoButtonVariant.Danger -> colors.danger
    }
    val foreground = when (variant) {
        TuyoButtonVariant.Primary -> colors.onAccent
        TuyoButtonVariant.Secondary -> colors.textPrimary
        TuyoButtonVariant.Ghost -> colors.textSecondary
        TuyoButtonVariant.Danger -> colors.onAccent
    }
    var m = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = if (enabled) 1f else 0.42f
        }
        .height(34.dp)
        .clip(shape)
        .background(background)
        .then(
            if (variant == TuyoButtonVariant.Secondary) {
                Modifier.border(BorderStroke(1.dp, colors.hairlineStrong), shape)
            } else {
                Modifier
            },
        )
    if (enabled) {
        m = m.clickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
    }
    Row(
        modifier = m.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
            maxLines = 1,
        )
    }
}

enum class TuyoButtonVariant { Primary, Secondary, Ghost, Danger }

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalTuyoColors.current.textSecondary,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
fun EvidenceTag(level: EvidenceLevel, modifier: Modifier = Modifier) {
    val colors = LocalTuyoColors.current
    val tint = when (level) {
        EvidenceLevel.RESEARCH -> colors.success
        EvidenceLevel.ANECDOTE -> colors.textSecondary
        EvidenceLevel.DEBATED -> colors.warning
    }
    val label = when (level) {
        EvidenceLevel.RESEARCH -> stringResource(R.string.tactics_evidence_research)
        EvidenceLevel.ANECDOTE -> stringResource(R.string.tactics_evidence_anecdote)
        EvidenceLevel.DEBATED -> stringResource(R.string.tactics_evidence_debated)
    }
    Tag(text = label, modifier = modifier, color = tint)
}

@Composable
fun SubjectTile(subject: Subject, modifier: Modifier = Modifier) {
    val tint = subjectTint(subject)
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(tint.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = subject.shortLabel,
            color = tint,
            fontFamily = TuyoFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 10.sp,
            letterSpacing = 0.3.sp,
        )
    }
}

@Composable
fun subjectTint(subject: Subject): Color {
    val colors = LocalTuyoColors.current
    return when (subject) {
        Subject.MATEMATIK -> colors.accent
        Subject.FIZIK -> Color(0xFF5AB7D4)
        Subject.TURKCE -> Color(0xFFE0915C)
        Subject.KIMYA -> colors.success
        Subject.BIYOLOJI -> Color(0xFFB58AE0)
        Subject.GEOMETRI -> colors.warning
        Subject.TARIH -> Color(0xFFC9A15B)
        Subject.COGRAFYA -> Color(0xFF6DC49A)
        Subject.EDEBIYAT -> Color(0xFFD48BA8)
        Subject.FELSEFE -> Color(0xFF9B8CFF)
        Subject.DIN -> Color(0xFF7FB77E)
        Subject.INGILIZCE -> Color(0xFF58A6C9)
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalTuyoColors.current.hairline),
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = LocalTuyoColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (actionLabel != null && onAction != null) {
            TuyoTextAction(
                text = actionLabel,
                onClick = onAction,
                color = colors.textSecondary,
                trailingArrow = false,
            )
        }
    }
}

/**
 * The single text-link affordance used for section actions and card context.
 * Tappable areas stay at least 32dp tall, while the visual weight stays quiet.
 */
@Composable
fun TuyoTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color? = null,
    leadingIcon: ImageVector? = null,
    trailingArrow: Boolean = true,
) {
    val resolvedColor = color ?: LocalTuyoColors.current.accent
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = resolvedColor,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = resolvedColor,
            maxLines = 1,
        )
        if (trailingArrow) {
            Text(
                text = "→",
                style = MaterialTheme.typography.labelLarge,
                color = resolvedColor,
            )
        }
    }
}

/** Shared screen title block so every tab starts with the same hierarchy. */
@Composable
fun ScreenHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = "",
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = LocalTuyoColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButtonGhost(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = backContentDescription,
                onClick = onBack,
            )
            Spacer(Modifier.width(Space.sm))
        }
        Column(modifier = Modifier.weight(1f)) {
            Eyebrow(eyebrow)
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
        actions()
    }
}

/**
 * One capture action for the whole app. It replaces per-screen “add” buttons,
 * so the learner always finds the same control in the same place.
 */
@Composable
fun CaptureButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(Motion.fast, easing = Motion.standard),
        label = "capturePress",
    )
    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(44.dp)
            .clip(RoundedCornerShape(Radius.pill))
            .background(colors.accent)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = Space.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = colors.onAccent,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onAccent,
            maxLines = 1,
        )
    }
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
        color = color ?: LocalTuyoColors.current.textTertiary,
        modifier = modifier,
    )
}

@Composable
fun MasteryDots(
    level: Int,
    modifier: Modifier = Modifier,
    total: Int = 5,
) {
    val colors = LocalTuyoColors.current
    val description = stringResource(R.string.mistakes_mastery, level)
    Row(
        modifier = modifier.semantics {
            contentDescription = description
        },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(if (index < level) 5.dp else 4.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(if (index < level) colors.accent else colors.hairlineStrong),
            )
        }
    }
}

@Composable
fun ProgressLine(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    val colors = LocalTuyoColors.current
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(Motion.slow, easing = Motion.standard),
        label = "progress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(Radius.pill))
            .background(colors.hairline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(3.dp)
                .clip(RoundedCornerShape(Radius.pill))
                .background(color ?: colors.accent),
        )
    }
}

@Composable
fun IconButtonGhost(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (pressed) colors.selection else Color.Transparent)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun MetaText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = LocalTuyoColors.current.textTertiary,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
