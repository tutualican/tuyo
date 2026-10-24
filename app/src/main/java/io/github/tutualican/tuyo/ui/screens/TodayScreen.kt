package io.github.tutualican.tuyo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.asUserData
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.MotivationQuote
import io.github.tutualican.tuyo.domain.model.PlanTask
import io.github.tutualican.tuyo.domain.model.StudyData
import io.github.tutualican.tuyo.domain.model.Tactic
import io.github.tutualican.tuyo.domain.questionCountOn
import io.github.tutualican.tuyo.domain.studySessionsOn
import io.github.tutualican.tuyo.domain.topicStats
import io.github.tutualican.tuyo.domain.weeklySummary
import io.github.tutualican.tuyo.ui.components.ProgressLine
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
    onAddNet: () -> Unit,
    onOpenMotivation: () -> Unit,
    onOpenWork: () -> Unit = {},
    onOpenTopics: () -> Unit = {},
    onAddQuestion: () -> Unit = {},
    onTogglePlanTask: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = LocalTuyoColors.current
    val today = LocalDate.now()
    val user = remember(data) { data.asUserData() }
    val due = data.dueMistakes
    val nextMove = due.firstOrNull()
    val pending = due.drop(1).take(3)
    val featured = data.tactics.firstOrNull { it.id == data.featuredTacticId }
        ?: data.tactics.first()
    val todayQuote = data.quotes[
        Math.floorMod(today.dayOfYear, data.quotes.size)
    ]
    val todayTasks = user.planTasks.filter {
        it.date == today && it.period == io.github.tutualican.tuyo.domain.model.PlanPeriod.DAILY
    }
    val todayQuestions = user.questionCountOn(today)
    val todayMinutes = user.studySessionsOn(today).sumOf { it.minutes }
    val allTasksDone = todayTasks.isNotEmpty() && todayTasks.all { it.completed }
    val reachedDailyGoal =
        todayQuestions >= data.profile.dailyQuestionTarget &&
            todayMinutes >= data.profile.dailyMinuteTarget

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 96.dp),
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
        item { CountdownCard(data = data) }
        item {
            TodaySnapshotCard(
                data = data,
                onAddQuestion = onAddQuestion,
                onOpenWork = onOpenWork,
            )
        }
        item {
            TodayPlanCard(
                tasks = todayTasks,
                isRestDay = today.toEpochDay() in data.restDayEpochDays,
                onTogglePlanTask = onTogglePlanTask,
                onOpenWork = onOpenWork,
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
                    data.mistakes.isEmpty() -> FirstStepCard()
                    else -> CaughtUpCard()
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
            WeakTopicCard(data = data, onOpenTopics = onOpenTopics)
        }
        item {
            WeeklySummaryCard(data = data, onOpenWork = onOpenWork)
        }
        item {
            MotivationCard(
                quote = todayQuote,
                onClick = onOpenMotivation,
            )
        }
        item { FeaturedTacticCard(tactic = featured, onClick = { onOpenTactic(featured.id) }) }
        if (allTasksDone || reachedDailyGoal) {
            item { DayPauseCard() }
        }
    }
}

@Composable
private fun DayPauseCard() {
    val colors = LocalTuyoColors.current
    TuyoCard(tint = colors.freshWash) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(colors.success.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(Space.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.today_pause_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.today_pause_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun CountdownCard(data: StudyData) {
    val colors = LocalTuyoColors.current
    val today = LocalDate.now()
    val days = java.time.temporal.ChronoUnit.DAYS.between(today, data.profile.examDate)
    TuyoCard(highlighted = days in 0..30, tint = colors.warmWash) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow("YKS'YE KALAN")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        days > 0 -> "$days gün"
                        days == 0L -> "Bugün"
                        else -> "Sınav tarihi geçti"
                    },
                    style = NumericStyle,
                    color = colors.textPrimary,
                )
                MetaText(
                    data.profile.examDate.format(
                        java.time.format.DateTimeFormatter.ofPattern(
                            "d MMMM yyyy",
                            Locale.forLanguageTag("tr"),
                        ),
                    ) + " · tarihi ayarlardan değiştir",
                )
            }
            Tag(
                text = if (days in 0..30) "Son düzlük" else "Sakin ilerle",
                color = if (days in 0..30) colors.warning else colors.success,
            )
        }
    }
}

@Composable
private fun TodaySnapshotCard(
    data: StudyData,
    onAddQuestion: () -> Unit,
    onOpenWork: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val today = LocalDate.now()
    val user = remember(data) { data.asUserData() }
    val questions = user.questionCountOn(today)
    val minutes = user.studySessionsOn(today).sumOf { it.minutes }
    val tasks = user.planTasks.filter {
        it.date == today && it.period == io.github.tutualican.tuyo.domain.model.PlanPeriod.DAILY
    }
    TuyoCard(tint = colors.coolWash) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("BUGÜNÜN ÖZETİ")
            Spacer(Modifier.weight(1f))
            Tag(
                text = "${tasks.count { it.completed }}/${tasks.size} görev",
                color = colors.textTertiary,
            )
        }
        Spacer(Modifier.height(Space.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            MetricBlock("$questions", "soru", Modifier.weight(1f))
            MetricBlock("$minutes", "dakika", Modifier.weight(1f))
            MetricBlock("${data.streakDays}", "gün seri", Modifier.weight(1f))
        }
        Spacer(Modifier.height(Space.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            TuyoButton(
                text = "Soru ekle",
                onClick = onAddQuestion,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Secondary,
            )
            TuyoButton(
                text = "Çalışma",
                onClick = onOpenWork,
                modifier = Modifier.weight(1f),
                variant = TuyoButtonVariant.Ghost,
            )
        }
    }
}

@Composable
private fun MetricBlock(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = LocalTuyoColors.current
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.textPrimary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun TodayPlanCard(
    tasks: List<PlanTask>,
    isRestDay: Boolean,
    onTogglePlanTask: (String) -> Unit,
    onOpenWork: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    TuyoCard(
        highlighted = isRestDay,
        tint = if (isRestDay) colors.freshWash else null,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("GÜNLÜK PLAN")
            Spacer(Modifier.weight(1f))
            if (isRestDay) Tag(text = "DİNLENME", color = colors.warning)
        }
        Spacer(Modifier.height(Space.sm))
        if (isRestDay) {
            Text(
                text = "Bugün dinlenme günü. İstersen küçük bir tekrar; yoksa gerçekten dinlen.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        } else if (tasks.isEmpty()) {
            Text(
                text = "Bugün için görev yok. Takvimden veya plandan ekleyebilirsin.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        } else {
            tasks.take(3).forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(if (task.completed) colors.accent else colors.surface)
                            .clickable { onTogglePlanTask(task.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (task.completed) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (task.completed) colors.textTertiary else colors.textPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    MetaText("${task.target}")
                }
            }
        }
        Spacer(Modifier.height(Space.sm))
        TuyoButton(
            text = "Planı aç",
            onClick = onOpenWork,
            modifier = Modifier.fillMaxWidth(),
            variant = TuyoButtonVariant.Ghost,
        )
    }
}

@Composable
private fun WeakTopicCard(data: StudyData, onOpenTopics: () -> Unit) {
    val colors = LocalTuyoColors.current
    val weak = remember(data.mistakes, data.topicProgress) {
        data.mistakes.topicStats(data.topics, progress = data.topicProgress)
            .filter { it.isWeak }
            .sortedWith(
                compareByDescending<io.github.tutualican.tuyo.domain.TopicStat> { it.dueCount }
                    .thenBy { it.mastery ?: 99 },
            )
            .firstOrNull()
    } ?: return
    TuyoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow("ODAK KONUSU")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = weak.topic.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                MetaText(
                    when {
                        weak.status == io.github.tutualican.tuyo.domain.model.TopicStatus.NEEDS_REVIEW ->
                            "Tekrar gerekli olarak işaretledin."
                        weak.dueCount > 0 -> "${weak.dueCount} yanlış tekrar bekliyor."
                        else -> "Defterindeki kayıtlar bu konuyu işaret ediyor."
                    },
                )
            }
            TuyoButton(
                text = "Konular",
                onClick = onOpenTopics,
                variant = TuyoButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun WeeklySummaryCard(data: StudyData, onOpenWork: () -> Unit) {
    val colors = LocalTuyoColors.current
    val today = LocalDate.now()
    val summary = remember(data) { data.asUserData().weeklySummary(today) }
    TuyoCard(tint = colors.freshWash) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("BU HAFTA")
            Spacer(Modifier.weight(1f))
            MetaText(
                "${summary.start.format(java.time.format.DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr")))} – " +
                    summary.end.format(java.time.format.DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr"))),
            )
        }
        Spacer(Modifier.height(Space.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            MetricBlock("${summary.questions}", "soru", Modifier.weight(1f))
            MetricBlock("${summary.minutes}", "dakika", Modifier.weight(1f))
            MetricBlock("${summary.exams}", "deneme", Modifier.weight(1f))
        }
        Spacer(Modifier.height(Space.sm))
        MetaText("${netText(summary.net)} net · ${summary.sessions} blok · ${summary.reviews} tekrar")
        Spacer(Modifier.height(Space.sm))
        TuyoButton(
            text = "Haftayı çalışmada aç",
            onClick = onOpenWork,
            modifier = Modifier.fillMaxWidth(),
            variant = TuyoButtonVariant.Ghost,
        )
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
                color = colors.success,
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
                NetAddAction(onAddNet = onAddNet)
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
        }
        return
    }

    TuyoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(stringResource(R.string.today_net_label))
            Spacer(Modifier.width(Space.sm))
            Tag(text = latest.exam.label, color = colors.textSecondary)
            Spacer(Modifier.weight(1f))
            NetAddAction(onAddNet = onAddNet)
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
        MetaText(stringResource(R.string.today_net_count, data.nets.size))
    }
}

@Composable
private fun NetAddAction(onAddNet: () -> Unit) {
    TuyoButton(
        text = stringResource(R.string.today_net_add),
        onClick = onAddNet,
        leadingIcon = Icons.Filled.Add,
        variant = TuyoButtonVariant.Ghost,
    )
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
private fun FirstStepCard() {
    val colors = LocalTuyoColors.current
    TuyoCard(highlighted = true, tint = colors.coolWash) {
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
    }
}

@Composable
private fun CaughtUpCard() {
    val colors = LocalTuyoColors.current
    TuyoCard(tint = colors.freshWash) {
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
            MetaText(text = mistake.questionRef, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.today_open_review) + "  →",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
            )
        }
    }
}

@Composable
private fun MotivationCard(quote: MotivationQuote, onClick: () -> Unit) {
    val colors = LocalTuyoColors.current
    Column {
        SectionHeader(title = stringResource(R.string.today_motivation_title))
        Spacer(Modifier.height(Space.md))
        TuyoCard(onClick = onClick, tint = colors.warmWash) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(stringResource(R.string.today_motivation_eyebrow))
                Spacer(Modifier.weight(1f))
                Tag(text = quote.theme.label, color = colors.accent)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "“${quote.text}”",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = quote.source,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.today_motivation_action) + "  →",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
            )
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
                text = stringResource(R.string.today_tactic_open) + "  →",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
            )
        }
    }
}
