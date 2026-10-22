package io.github.tutualican.tuyo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.StudyData
import io.github.tutualican.tuyo.domain.model.Tactic
import io.github.tutualican.tuyo.ui.components.EvidenceTag
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.IconButtonGhost
import io.github.tutualican.tuyo.ui.components.MetaText
import io.github.tutualican.tuyo.ui.components.MistakeRow
import io.github.tutualican.tuyo.ui.components.SectionHeader
import io.github.tutualican.tuyo.ui.components.Sparkline
import io.github.tutualican.tuyo.ui.components.Tag
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.components.netText
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.NumericStyle
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@Composable
fun TodayScreen(
    data: StudyData,
    onOpenMistake: (String) -> Unit,
    onSeeAllMistakes: () -> Unit,
    onOpenTactic: (String) -> Unit,
    onOpenPalette: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddMistake: () -> Unit,
    onAddNet: () -> Unit,
    onOpenMotivation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    val due = data.dueMistakes
    val nextMove = due.firstOrNull()
    val pending = due.drop(1).take(3)
    val featured = data.tactics.firstOrNull { it.id == data.featuredTacticId }
        ?: data.tactics.first()
    val todayNote = data.motivation[
        Math.floorMod(LocalDate.now().dayOfYear, data.motivation.size)
    ]

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(Space.xxl),
    ) {
        item {
            TodayHeader(
                name = data.learnerName,
                track = data.profile.track?.label,
                streak = data.streakDays,
                onOpenSettings = onOpenSettings,
                onOpenPalette = onOpenPalette,
            )
        }
        item {
            NetCard(
                data = data,
                onAddNet = onAddNet,
            )
        }
        item { LoopCard() }
        item {
            Column {
                SectionHeader(title = stringResource(R.string.today_next_move))
                Spacer(Modifier.height(Space.md))
                when {
                    nextMove != null -> NextMoveCard(
                        mistake = nextMove,
                        onClick = { onOpenMistake(nextMove.id) },
                    )
                    data.mistakes.isEmpty() -> FirstStepCard(onAddMistake = onAddMistake)
                    else -> CaughtUpCard(onAddMistake = onAddMistake)
                }
            }
        }
        if (pending.isNotEmpty()) {
            item {
                Column {
                    SectionHeader(
                        title = stringResource(R.string.today_pending_title),
                        actionLabel = stringResource(R.string.today_pending_action),
                        onAction = onSeeAllMistakes,
                    )
                    Spacer(Modifier.height(2.dp))
                    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = 2.dp)) {
                        pending.forEachIndexed { index, mistake ->
                            MistakeRow(mistake = mistake, onClick = { onOpenMistake(mistake.id) })
                            if (index != pending.lastIndex) Hairline()
                        }
                    }
                }
            }
        }
        item {
            MotivationCard(
                text = todayNote.text,
                theme = todayNote.theme.label,
                onClick = onOpenMotivation,
            )
        }
        item { FeaturedTacticCard(tactic = featured, onClick = { onOpenTactic(featured.id) }) }
    }
}

@Composable
private fun TodayHeader(
    name: String,
    track: String?,
    streak: Int,
    onOpenSettings: () -> Unit,
    onOpenPalette: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> stringResource(R.string.today_greeting_morning)
        in 12..17 -> stringResource(R.string.today_greeting_afternoon)
        else -> stringResource(R.string.today_greeting_evening)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Eyebrow(
                text = listOfNotNull(
                    stringResource(R.string.today_eyebrow),
                    track?.uppercase(Locale.forLanguageTag("tr")),
                ).joinToString(" · "),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$greeting, $name",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
        }
        if (streak > 0) {
            Tag(
                text = stringResource(R.string.today_streak, streak),
                color = colors.warning,
            )
            Spacer(Modifier.width(Space.sm))
        }
        IconButtonGhost(
            icon = Icons.Filled.Search,
            contentDescription = stringResource(R.string.command_open),
            onClick = onOpenPalette,
        )
        IconButtonGhost(
            icon = Icons.Filled.Settings,
            contentDescription = stringResource(R.string.settings_title),
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun NetCard(data: StudyData, onAddNet: () -> Unit) {
    val colors = LocalTuyoColors.current
    val latest = data.latestNet
    val delta = data.netDelta

    if (latest == null) {
        TuyoCard(highlighted = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(stringResource(R.string.today_net_label))
                Spacer(Modifier.weight(1f))
                Tag(text = "TYT · AYT · YDT", color = colors.textTertiary)
            }
            Spacer(Modifier.height(Space.md))
            Text(
                text = stringResource(R.string.today_net_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.today_net_empty_body),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(Space.md))
            TuyoButton(
                text = stringResource(R.string.today_net_add),
                onClick = onAddNet,
                leadingIcon = Icons.Filled.Add,
                variant = TuyoButtonVariant.Secondary,
            )
        }
        return
    }

    TuyoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.today_net_label))
            Spacer(Modifier.weight(1f))
            Tag(text = latest.exam.label, color = colors.textSecondary)
        }
        Spacer(Modifier.height(Space.md))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = netText(latest.net),
                style = NumericStyle,
                color = colors.textPrimary,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "net",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 7.dp),
            )
            Spacer(Modifier.weight(1f))
            if (delta != null) {
                val positive = delta >= 0
                Tag(
                    text = (if (positive) "+" else "") + netText(delta),
                    color = if (positive) colors.success else colors.danger,
                )
            }
        }
        Spacer(Modifier.height(Space.md))
        Sparkline(
            values = data.netTrend.map { it.net },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
        )
        Spacer(Modifier.height(Space.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MetaText(stringResource(R.string.today_net_count, data.nets.size))
            Spacer(Modifier.weight(1f))
            TuyoButton(
                text = stringResource(R.string.today_net_add),
                onClick = onAddNet,
                leadingIcon = Icons.Filled.Add,
                variant = TuyoButtonVariant.Ghost,
            )
        }
    }
}

@Composable
private fun LoopCard() {
    val colors = LocalTuyoColors.current
    TuyoCard(contentPadding = PaddingValues(horizontal = Space.lg, vertical = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LoopStep(index = "01", label = stringResource(R.string.today_loop_y))
            LoopArrow()
            LoopStep(index = "02", label = stringResource(R.string.today_loop_k))
            LoopArrow()
            LoopStep(index = "03", label = stringResource(R.string.today_loop_s))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.today_loop_caption),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun LoopStep(index: String, label: String) {
    val colors = LocalTuyoColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = index,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = colors.accent,
            )
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun LoopArrow() {
    Text(
        text = "›",
        style = MaterialTheme.typography.titleLarge,
        color = LocalTuyoColors.current.textTertiary,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}

@Composable
private fun FirstStepCard(onAddMistake: () -> Unit) {
    val colors = LocalTuyoColors.current
    TuyoCard(highlighted = true) {
        Eyebrow(stringResource(R.string.today_first_eyebrow), color = colors.accent)
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.today_first_title),
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.today_first_body),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(14.dp))
        TuyoButton(
            text = stringResource(R.string.mistakes_add),
            onClick = onAddMistake,
            leadingIcon = Icons.Filled.Add,
        )
    }
}

@Composable
private fun CaughtUpCard(onAddMistake: () -> Unit) {
    val colors = LocalTuyoColors.current
    TuyoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(colors.success.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(14.dp),
                )
            }
            Spacer(Modifier.width(Space.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.today_caught_up_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.today_caught_up_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        TuyoButton(
            text = stringResource(R.string.mistakes_add),
            onClick = onAddMistake,
            leadingIcon = Icons.Filled.Add,
            variant = TuyoButtonVariant.Secondary,
        )
    }
}

@Composable
private fun NextMoveCard(mistake: Mistake, onClick: () -> Unit) {
    val colors = LocalTuyoColors.current
    TuyoCard(highlighted = true, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.today_priority), color = colors.accent)
            Spacer(Modifier.weight(1f))
            Tag(text = mistake.errorKind.label, color = colors.danger)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = mistake.topicTitle,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = mistake.note.ifBlank { mistake.errorKind.advice },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TuyoButton(
                text = stringResource(R.string.today_open_review),
                onClick = onClick,
                variant = TuyoButtonVariant.Secondary,
            )
            Spacer(Modifier.width(Space.md))
            MetaText(text = mistake.questionRef)
        }
    }
}

@Composable
private fun MotivationCard(text: String, theme: String, onClick: () -> Unit) {
    val colors = LocalTuyoColors.current
    Column {
        SectionHeader(
            title = stringResource(R.string.today_motivation_title),
            actionLabel = stringResource(R.string.today_motivation_action),
            onAction = onClick,
        )
        Spacer(Modifier.height(Space.md))
        TuyoCard(onClick = onClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(stringResource(R.string.today_motivation_eyebrow))
                Spacer(Modifier.weight(1f))
                Tag(text = theme, color = colors.accent)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
private fun FeaturedTacticCard(tactic: Tactic, onClick: () -> Unit) {
    val colors = LocalTuyoColors.current
    Column {
        SectionHeader(
            title = stringResource(R.string.today_tactic_title),
            actionLabel = stringResource(R.string.today_tactic_action),
            onAction = onClick,
        )
        Spacer(Modifier.height(Space.md))
        TuyoCard(onClick = onClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EvidenceTag(tactic.evidence)
                Spacer(Modifier.weight(1f))
                MetaText(stringResource(R.string.tactics_minutes, tactic.minutes))
            }
            Spacer(Modifier.height(10.dp))
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.today_tactic_open) + "  →",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
            )
        }
    }
}
