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
import io.github.tutualican.tuyo.ui.components.MasteryDots
import io.github.tutualican.tuyo.ui.components.MetaText
import io.github.tutualican.tuyo.ui.components.MistakeRow
import io.github.tutualican.tuyo.ui.components.SectionHeader
import io.github.tutualican.tuyo.ui.components.Sparkline
import io.github.tutualican.tuyo.ui.components.Tag
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.components.netText
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.NumericStyle
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalTime

@Composable
fun TodayScreen(
    data: StudyData,
    onOpenMistake: (String) -> Unit,
    onSeeAllMistakes: () -> Unit,
    onOpenTactic: (String) -> Unit,
    onOpenPalette: () -> Unit,
    onAddMistake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    val pending = data.mistakes
        .filter { it.isDue(java.time.LocalDate.now()) }
        .take(3)
    val featured = data.tactics.firstOrNull { it.id == data.featuredTacticId }
        ?: data.tactics.first()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(Space.xxl),
    ) {
        item {
            TodayHeader(
                name = data.learnerName,
                streak = data.streakDays,
                onOpenPalette = onOpenPalette,
            )
        }
        item { NetCard(data) }
        item { LoopCard() }
        item {
            Column {
                SectionHeader(title = stringResource(R.string.today_next_move))
                Spacer(Modifier.height(Space.md))
                val nextMove = pending.firstOrNull()
                if (nextMove == null) {
                    CaughtUpCard(onAddMistake = onAddMistake)
                } else {
                    NextMoveCard(
                        mistake = nextMove,
                        onClick = { onOpenMistake(nextMove.id) },
                    )
                }
            }
        }
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
        item { FeaturedTacticCard(tactic = featured, onClick = { onOpenTactic(featured.id) }) }
    }
}

@Composable
private fun TodayHeader(name: String, streak: Int, onOpenPalette: () -> Unit) {
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
            Eyebrow(stringResource(R.string.today_eyebrow))
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$greeting, $name",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
        }
        Tag(
            text = stringResource(R.string.today_streak, streak),
            color = colors.warning,
        )
        Spacer(Modifier.width(Space.sm))
        IconButtonGhost(
            icon = Icons.Filled.Search,
            contentDescription = stringResource(R.string.command_open),
            onClick = onOpenPalette,
        )
    }
}

@Composable
private fun NetCard(data: StudyData) {
    val colors = LocalTuyoColors.current
    val latest = data.latestNet
    val delta = data.netDelta
    TuyoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.today_net_label))
            Spacer(Modifier.weight(1f))
            Tag(text = "TYT", color = colors.textSecondary)
        }
        Spacer(Modifier.height(Space.md))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = latest?.let(::netText) ?: "Ã¢â‚¬â€",
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
            values = data.nets.map { it.net },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
        )
        Spacer(Modifier.height(Space.md))
        Row {
            MetaText("Son ${data.nets.size} deneme")
            Spacer(Modifier.weight(1f))
            MetaText("Hedef 45 net")
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
            LoopStep(index = "01", letter = "Y", label = stringResource(R.string.today_loop_y))
            LoopArrow()
            LoopStep(index = "02", letter = "K", label = stringResource(R.string.today_loop_k))
            LoopArrow()
            LoopStep(index = "03", letter = "S", label = stringResource(R.string.today_loop_s))
            Spacer(Modifier.weight(1f))
            MasteryDots(level = 3)
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
private fun LoopStep(index: String, letter: String, label: String) {
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
                text = letter,
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
            )
        }
        Spacer(Modifier.width(7.dp))
        Column {
            Text(
                text = index,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.6.sp),
                color = colors.textTertiary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
private fun LoopArrow() {
    Text(
        text = "Ã¢â‚¬Âº",
        style = MaterialTheme.typography.titleLarge,
        color = LocalTuyoColors.current.textTertiary,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
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
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(14.dp),
                )
            }
            Spacer(Modifier.width(Space.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BugÃƒÂ¼nlÃƒÂ¼k tamam",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Tekrar kuyruÃ„Å¸u boÃ…Å¸. Yeni bir yanlÃ„Â±Ã…Å¸ ekleyerek dÃƒÂ¶ngÃƒÂ¼yÃƒÂ¼ sÃƒÂ¼rdÃƒÂ¼r.",
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
            variant = io.github.tutualican.tuyo.ui.components.TuyoButtonVariant.Secondary,
        )
    }
}

@Composable
private fun NextMoveCard(mistake: Mistake, onClick: () -> Unit) {
    val colors = LocalTuyoColors.current
    TuyoCard(highlighted = true, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("Ãƒâ€“NCELÃ„Â°K", color = colors.accent)
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
            text = mistake.note,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TuyoButton(
                text = "Konuyu aÃƒÂ§",
                onClick = onClick,
                variant = io.github.tutualican.tuyo.ui.components.TuyoButtonVariant.Secondary,
            )
            Spacer(Modifier.width(Space.md))
            MetaText(text = mistake.questionRef)
        }
    }
}

@Composable
private fun FeaturedTacticCard(tactic: Tactic, onClick: () -> Unit) {
    val colors = LocalTuyoColors.current
    Column {
        SectionHeader(title = stringResource(R.string.today_tactic_title))
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
                text = stringResource(R.string.today_tactic_action) + "  Ã¢â€ â€™",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
            )
        }
    }
}
