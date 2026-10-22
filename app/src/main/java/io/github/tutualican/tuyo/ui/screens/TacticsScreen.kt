package io.github.tutualican.tuyo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.Tactic
import io.github.tutualican.tuyo.ui.components.EvidenceTag
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.MetaText
import io.github.tutualican.tuyo.ui.components.Tag
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.Motion
import io.github.tutualican.tuyo.ui.theme.Space

@Composable
fun TacticsScreen(
    tactics: List<Tactic>,
    modifier: Modifier = Modifier,
    focusedId: String? = null,
) {
    val colors = LocalTuyoColors.current
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(focusedId) {
        if (focusedId != null && tactics.any { it.id == focusedId }) {
            expanded = focusedId
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
    ) {
        item {
            Column {
                Eyebrow("KÜTÜPHANE")
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.tactics_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.tactics_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                    Spacer(Modifier.width(Space.md))
                    TuyoButton(
                        text = stringResource(R.string.tactics_submit),
                        onClick = {},
                        enabled = false,
                        variant = TuyoButtonVariant.Secondary,
                    )
                }
            }
        }
        item {
            TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Tag(text = "v0.1", color = colors.textTertiary)
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = stringResource(R.string.tactics_bundled_note),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        items(tactics, key = { it.id }) { tactic ->
            TacticCard(
                tactic = tactic,
                expanded = expanded == tactic.id,
                onToggle = {
                    expanded = if (expanded == tactic.id) null else tactic.id
                },
            )
        }
    }
}

@Composable
private fun TacticCard(
    tactic: Tactic,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(Motion.base, easing = Motion.standard),
        label = "tacticChevron",
    )
    TuyoCard(onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EvidenceTag(tactic.evidence)
            Spacer(Modifier.weight(1f))
            MetaText(
                "${stringResource(R.string.tactics_minutes, tactic.minutes)} · ${tactic.steps.size} adım",
            )
        }
        Spacer(Modifier.height(11.dp))
        Text(
            text = tactic.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = tactic.summary,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(14.dp))
                Hairline()
                tactic.steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.padding(vertical = Space.md),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = (index + 1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFeatureSettings = "tnum",
                            ),
                            color = colors.textTertiary,
                            modifier = Modifier
                                .width(22.dp)
                                .padding(top = 2.dp),
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(io.github.tutualican.tuyo.ui.theme.Radius.sm))
                .background(colors.selection.copy(alpha = 0.5f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (expanded) "Adımları kapat" else "Adımları gör",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier
                    .size(15.dp)
                    .rotate(rotation),
            )
        }
    }
}
