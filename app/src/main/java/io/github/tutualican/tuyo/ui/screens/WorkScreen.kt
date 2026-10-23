package io.github.tutualican.tuyo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tutualican.tuyo.data.StudyTimer
import io.github.tutualican.tuyo.data.StudyTimerState
import io.github.tutualican.tuyo.domain.PlanSuggestion
import io.github.tutualican.tuyo.domain.TopicStat
import io.github.tutualican.tuyo.domain.asUserData
import io.github.tutualican.tuyo.domain.currentNetFor
import io.github.tutualican.tuyo.domain.overduePlanTasks
import io.github.tutualican.tuyo.domain.planSuggestions
import io.github.tutualican.tuyo.domain.planTasksOn
import io.github.tutualican.tuyo.domain.questionCountOn
import io.github.tutualican.tuyo.domain.studySessionsOn
import io.github.tutualican.tuyo.domain.topicStats
import io.github.tutualican.tuyo.domain.weekStart
import io.github.tutualican.tuyo.domain.weeklyPlanTasks
import io.github.tutualican.tuyo.domain.weeklySummary
import io.github.tutualican.tuyo.domain.model.ExamNet
import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.domain.model.NetGoal
import io.github.tutualican.tuyo.domain.model.PlanMetric
import io.github.tutualican.tuyo.domain.model.PlanPeriod
import io.github.tutualican.tuyo.domain.model.PlanTask
import io.github.tutualican.tuyo.domain.model.QuestionLog
import io.github.tutualican.tuyo.domain.model.StudyData
import io.github.tutualican.tuyo.domain.model.StudySession
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.TimerMode
import io.github.tutualican.tuyo.ui.components.Eyebrow
import io.github.tutualican.tuyo.ui.components.Hairline
import io.github.tutualican.tuyo.ui.components.MetaText
import io.github.tutualican.tuyo.ui.components.ProgressLine
import io.github.tutualican.tuyo.ui.components.ScreenHeader
import io.github.tutualican.tuyo.ui.components.SectionHeader
import io.github.tutualican.tuyo.ui.components.SubjectTile
import io.github.tutualican.tuyo.ui.components.Tag
import io.github.tutualican.tuyo.ui.components.TuyoButton
import io.github.tutualican.tuyo.ui.components.TuyoButtonVariant
import io.github.tutualican.tuyo.ui.components.TuyoCard
import io.github.tutualican.tuyo.ui.components.TuyoChoiceChip
import io.github.tutualican.tuyo.ui.components.TuyoSegmented
import io.github.tutualican.tuyo.ui.components.TuyoTextAction
import io.github.tutualican.tuyo.ui.components.netText
import io.github.tutualican.tuyo.ui.components.subjectTint
import io.github.tutualican.tuyo.ui.theme.LocalTuyoColors
import io.github.tutualican.tuyo.ui.theme.NumericStyle
import io.github.tutualican.tuyo.ui.theme.Radius
import io.github.tutualican.tuyo.ui.theme.Space
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private enum class WorkSection(val label: String) {
    CALENDAR("Takvim"),
    PLAN("Plan"),
    TIMER("Sayaç"),
    EXAMS("Denemeler"),
}

@Composable
fun WorkScreen(
    data: StudyData,
    onAddQuestion: (LocalDate) -> Unit,
    onDeleteQuestionLog: (String) -> Unit,
    onAddPlanTask: (PlanPeriod, LocalDate) -> Unit,
    onTogglePlanTask: (String) -> Unit,
    onDeletePlanTask: (String) -> Unit,
    onCarryOver: () -> Unit,
    onToggleRestDay: (LocalDate) -> Unit,
    onApplySuggestions: () -> Unit,
    onStartTimer: (TimerMode, Subject?, Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onAddExam: () -> Unit,
    onAnalyzeExam: (ExamNet) -> Unit,
    onAddNetGoal: () -> Unit,
    onDeleteNetGoal: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sectionName by rememberSaveable { mutableStateOf(WorkSection.CALENDAR.name) }
    val section = WorkSection.entries.firstOrNull { it.name == sectionName } ?: WorkSection.CALENDAR

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Space.xl,
            end = Space.xl,
            top = Space.lg,
            bottom = 104.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
    ) {
        item {
            ScreenHeader(
                eyebrow = "ÇALIŞMA",
                title = "Çalışma",
                subtitle = "Takvim, plan, sayaç ve denemeler tek yerde.",
            )
        }
        item {
            TuyoSegmented(
                labels = WorkSection.entries.map { it.label },
                selectedIndex = WorkSection.entries.indexOf(section),
                onSelect = { sectionName = WorkSection.entries[it].name },
            )
        }
        item {
            when (section) {
                WorkSection.CALENDAR -> CalendarContent(data, onAddQuestion, onDeleteQuestionLog)
                WorkSection.PLAN -> PlanContent(
                    data = data,
                    onAddPlanTask = onAddPlanTask,
                    onTogglePlanTask = onTogglePlanTask,
                    onDeletePlanTask = onDeletePlanTask,
                    onCarryOver = onCarryOver,
                    onToggleRestDay = onToggleRestDay,
                    onApplySuggestions = onApplySuggestions,
                )
                WorkSection.TIMER -> TimerContent(
                    data = data,
                    onStartTimer = onStartTimer,
                    onPauseTimer = onPauseTimer,
                    onResumeTimer = onResumeTimer,
                    onStopTimer = onStopTimer,
                    onResetTimer = onResetTimer,
                )
                WorkSection.EXAMS -> ExamsContent(
                    data = data,
                    onAddExam = onAddExam,
                    onAnalyzeExam = onAnalyzeExam,
                    onAddNetGoal = onAddNetGoal,
                    onDeleteNetGoal = onDeleteNetGoal,
                    onStartSimulation = { minutes ->
                        onStartTimer(TimerMode.EXAM, null, minutes)
                        sectionName = WorkSection.TIMER.name
                    },
                )
            }
        }
    }
}

@Composable
private fun CalendarContent(
    data: StudyData,
    onAddQuestion: (LocalDate) -> Unit,
    onDeleteQuestionLog: (String) -> Unit,
) {
    val colors = LocalTuyoColors.current
    val today = LocalDate.now()
    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var selectedEpoch by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    val month = runCatching { YearMonth.parse(monthText) }.getOrDefault(YearMonth.now())
    val selected = LocalDate.ofEpochDay(selectedEpoch)
    val logs = data.questionLogs.filter { it.date == selected }.sortedBy { it.subject.ordinal }
    val sessions = data.studySessions.filter { it.date == selected }
    val events = remember(data.questionLogs, data.studySessions, data.planTasks) {
        data.questionLogs.map { it.date }.toSet() +
            data.studySessions.map { it.date }.toSet() +
            data.planTasks.map { it.date }.toSet()
    }

    Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
        TuyoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("TAKVİM")
                Spacer(Modifier.weight(1f))
                ArrowButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Önceki ay") {
                    monthText = month.minusMonths(1).toString()
                }
                Text(
                    text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr"))),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = Space.sm),
                )
                ArrowButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Sonraki ay") {
                    monthText = month.plusMonths(1).toString()
                }
            }
            Spacer(Modifier.height(Space.md))
            Row(Modifier.fillMaxWidth()) {
                listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            CalendarGrid(
                month = month,
                selected = selected,
                today = today,
                eventDates = events,
                onSelect = { selectedEpoch = it.toEpochDay() },
            )
        }

        TuyoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow(
                        selected.format(
                            DateTimeFormatter.ofPattern("d MMMM EEEE", Locale.forLanguageTag("tr")),
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (logs.isEmpty()) "Henüz soru kaydı yok" else {
                            "${logs.sumOf { it.total }} soru · ${netText(logs.sumOf { it.net })} net"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                    if (sessions.isNotEmpty()) {
                        MetaText("${sessions.sumOf { it.minutes }} dk çalışma")
                    }
                }
                TuyoButton(
                    text = "Soru ekle",
                    onClick = { onAddQuestion(selected) },
                    variant = TuyoButtonVariant.Secondary,
                )
            }
        }

        SectionHeader(title = "Soru kayıtları")
        if (logs.isEmpty()) {
            TuyoCard {
                MetaText("Takvimden bir gün seç, sonra D/Y/B gir. Kayıt 20 saniyeden kısa sürer.")
            }
        } else {
            TuyoCard(contentPadding = PaddingValues(Space.sm)) {
                logs.forEachIndexed { index, log ->
                    QuestionLogRow(log = log, onDelete = { onDeleteQuestionLog(log.id) })
                    if (index != logs.lastIndex) Hairline(modifier = Modifier.padding(horizontal = Space.md))
                }
            }
        }

        if (sessions.isNotEmpty()) {
            SectionHeader(title = "Çalışma blokları")
            TuyoCard {
                sessions.forEach { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = session.subject?.label ?: "Genel",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        MetaText(session.mode.label)
                        Spacer(Modifier.width(Space.sm))
                        Text(
                            text = "${session.minutes} dk",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.accent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    eventDates: Set<LocalDate>,
    onSelect: (LocalDate) -> Unit,
) {
    val offset = month.atDay(1).dayOfWeek.value - 1
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val cells: List<LocalDate?> = List(offset) { null } + days
    val padded = cells + List((7 - cells.size % 7) % 7) { null }
    padded.chunked(7).forEach { week ->
        Row(Modifier.fillMaxWidth()) {
            week.forEach { date ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (date != null) {
                        CalendarDay(
                            date = date,
                            selected = date == selected,
                            today = date == today,
                            hasEvent = date in eventDates,
                            onClick = { onSelect(date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    selected: Boolean,
    today: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(Radius.sm))
            .background(
                when {
                    selected -> colors.accent
                    today -> colors.accentSoft
                    else -> Color.Transparent
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) colors.onAccent else colors.textPrimary,
            )
            if (hasEvent) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (selected) colors.onAccent else colors.accent),
                )
            }
        }
    }
}

@Composable
private fun ArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = LocalTuyoColors.current.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun QuestionLogRow(log: QuestionLog, onDelete: () -> Unit) {
    val colors = LocalTuyoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.sm, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubjectTile(subject = log.subject, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(Space.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.subject.label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
            )
            MetaText("${log.correct} D · ${log.wrong} Y · ${log.blank} B · ${log.total} soru")
        }
        Text(
            text = "${netText(log.net)} net",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
        )
        TuyoTextAction(
            text = "Sil",
            onClick = onDelete,
            color = colors.textTertiary,
            trailingArrow = false,
        )
    }
}

@Composable
private fun PlanContent(
    data: StudyData,
    onAddPlanTask: (PlanPeriod, LocalDate) -> Unit,
    onTogglePlanTask: (String) -> Unit,
    onDeletePlanTask: (String) -> Unit,
    onCarryOver: () -> Unit,
    onToggleRestDay: (LocalDate) -> Unit,
    onApplySuggestions: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val today = LocalDate.now()
    val user = remember(data) { data.asUserData() }
    val dailyTasks = user.planTasksOn(today)
    val weeklyTasks = user.weeklyPlanTasks(today)
    val rest = today.toEpochDay() in data.restDayEpochDays
    val overdue = user.overduePlanTasks(today)
    val suggestions = remember(data.planTasks, data.studySessions, data.nets) {
        user.planSuggestions(data.topics, today)
    }
    val questionsToday = user.questionCountOn(today)
    val minutesToday = user.studySessionsOn(today).sumOf { it.minutes }
    val week = user.weeklySummary(today)

    Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
        TuyoCard(highlighted = rest) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow("BUGÜN")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (rest) "Dinlenme günü" else "Hedefler ve görevler",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                }
                Tag(
                    text = if (rest) "MOLA" else "${dailyTasks.count { it.completed }}/${dailyTasks.size} görev",
                    color = if (rest) colors.warning else colors.accent,
                )
            }
            Spacer(Modifier.height(Space.md))
            GoalLine("Soru", questionsToday, data.profile.dailyQuestionTarget)
            Spacer(Modifier.height(Space.sm))
            GoalLine("Dakika", minutesToday, data.profile.dailyMinuteTarget)
            Spacer(Modifier.height(Space.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                TuyoButton(
                    text = if (rest) "Dinlenmeyi kaldır" else "Bugünü dinlenme ilan et",
                    onClick = { onToggleRestDay(today) },
                    modifier = Modifier.weight(1f),
                    variant = TuyoButtonVariant.Ghost,
                )
                if (overdue.isNotEmpty()) {
                    TuyoButton(
                        text = "${overdue.size} görevi devret",
                        onClick = onCarryOver,
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Secondary,
                    )
                }
            }
        }

        SectionHeader(
            title = "Günlük görevler",
            actionLabel = "Ekle",
            onAction = { onAddPlanTask(PlanPeriod.DAILY, today) },
        )
        if (dailyTasks.isEmpty()) {
            TuyoCard { MetaText("Bugün için görev yok. Küçük bir blokla başla.") }
        } else {
            TuyoCard {
                dailyTasks.forEach { task ->
                    TaskRow(
                        task = task,
                        onToggle = { onTogglePlanTask(task.id) },
                        onDelete = { onDeletePlanTask(task.id) },
                    )
                }
            }
        }

        SectionHeader(
            title = "Haftalık hedefler",
            actionLabel = "Ekle",
            onAction = { onAddPlanTask(PlanPeriod.WEEKLY, today) },
        )
        TuyoCard {
            GoalLine("Haftalık soru", week.questions, data.profile.weeklyQuestionTarget)
            Spacer(Modifier.height(Space.sm))
            GoalLine("Haftalık dakika", week.minutes, data.profile.weeklyMinuteTarget)
            if (weeklyTasks.isNotEmpty()) {
                Spacer(Modifier.height(Space.md))
                Hairline()
                weeklyTasks.forEach { task ->
                    TaskRow(
                        task = task,
                        onToggle = { onTogglePlanTask(task.id) },
                        onDelete = { onDeletePlanTask(task.id) },
                    )
                }
            }
        }

        if (suggestions.isNotEmpty()) {
            SectionHeader(title = "Önerilen plan")
            TuyoCard {
                suggestions.forEachIndexed { index, suggestion ->
                    SuggestionRow(suggestion)
                    if (index != suggestions.lastIndex) {
                        Spacer(Modifier.height(Space.sm))
                        Hairline()
                        Spacer(Modifier.height(Space.sm))
                    }
                }
                Spacer(Modifier.height(Space.md))
                TuyoButton(
                    text = "Önerileri plana ekle",
                    onClick = onApplySuggestions,
                    modifier = Modifier.fillMaxWidth(),
                    variant = TuyoButtonVariant.Secondary,
                )
            }
        }
    }
}

@Composable
private fun GoalLine(label: String, current: Int, target: Int) {
    val colors = LocalTuyoColors.current
    val fraction = if (target > 0) current.toFloat() / target else 0f
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$current / $target",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textPrimary,
        )
    }
    Spacer(Modifier.height(4.dp))
    ProgressLine(fraction = fraction)
}

@Composable
private fun TaskRow(
    task: PlanTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (task.completed) colors.accent else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (task.completed) colors.accent else colors.hairlineStrong,
                    shape = CircleShape,
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            if (task.completed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(Modifier.width(Space.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodySmall,
                color = if (task.completed) colors.textTertiary else colors.textPrimary,
            )
            val details = buildString {
                task.subject?.let { append("${it.label} · ") }
                append("${task.target} ${if (task.metric == PlanMetric.QUESTIONS) "soru" else "dk"}")
                task.carriedFrom?.let { append(" · ${it.format(DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr")))}'den") }
            }
            MetaText(details)
        }
        TuyoTextAction(
            text = "×",
            onClick = onDelete,
            color = colors.textTertiary,
            trailingArrow = false,
        )
    }
}

@Composable
private fun SuggestionRow(suggestion: PlanSuggestion) {
    val colors = LocalTuyoColors.current
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(colors.accent),
        )
        Spacer(Modifier.width(Space.sm))
        Column {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
            )
            Text(
                text = suggestion.reason,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun TimerContent(
    data: StudyData,
    onStartTimer: (TimerMode, Subject?, Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onResetTimer: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val timer by StudyTimer.state.collectAsStateWithLifecycle()
    var modeName by rememberSaveable { mutableStateOf(TimerMode.POMODORO.name) }
    var subjectName by rememberSaveable { mutableStateOf<String?>(null) }
    val mode = TimerMode.entries.firstOrNull { it.name == modeName } ?: TimerMode.POMODORO
    val subject = subjectName?.let { name -> Subject.entries.firstOrNull { it.name == name } }
    val today = LocalDate.now()
    val sessions = data.studySessions.filter { it.date == today }
    val minutes = sessions.sumOf { it.minutes }

    Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
        TimerCard(
            timer = timer,
            onPause = onPauseTimer,
            onResume = onResumeTimer,
            onStop = onStopTimer,
            onReset = onResetTimer,
        )

        if (!timer.running && timer.elapsedSeconds == 0) {
            TuyoCard {
                Eyebrow("YENİ SAYAÇ")
                Spacer(Modifier.height(Space.sm))
                TuyoSegmented(
                    labels = listOf("Pomodoro", "Serbest"),
                    selectedIndex = if (mode == TimerMode.POMODORO) 0 else 1,
                    onSelect = {
                        modeName = if (it == 0) TimerMode.POMODORO.name else TimerMode.FREE.name
                    },
                )
                Spacer(Modifier.height(Space.md))
                Text(
                    text = "Ders",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TuyoChoiceChip(
                        label = "Genel",
                        selected = subject == null,
                        onClick = { subjectName = null },
                    )
                    Subject.entries.forEach { option ->
                        TuyoChoiceChip(
                            label = option.label,
                            selected = subject == option,
                            onClick = { subjectName = option.name },
                        )
                    }
                }
                Spacer(Modifier.height(Space.md))
                TuyoButton(
                    text = if (mode == TimerMode.POMODORO) "25 dakika başlat" else "Serbest sayacı başlat",
                    onClick = {
                        onStartTimer(
                            mode,
                            subject,
                            if (mode == TimerMode.POMODORO) 25 else 0,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Filled.PlayArrow,
                )
            }
        }

        TuyoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow("BUGÜN")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$minutes dk · ${sessions.size} blok",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                }
                Tag(text = "${data.studySessions.size} toplam", color = colors.textTertiary)
            }
            if (sessions.isNotEmpty()) {
                Spacer(Modifier.height(Space.md))
                sessions.groupBy { it.subject }.forEach { (subject, items) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = subject?.label ?: "Genel",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${items.sumOf { it.minutes }} dk",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textPrimary,
                        )
                    }
                }
            }
        }

        SectionHeader(title = "Son bloklar")
        if (data.studySessions.isEmpty()) {
            TuyoCard { MetaText("Henüz çalışma kaydı yok. Pomodoro ile başla.") }
        } else {
            TuyoCard {
                data.studySessions.take(6).forEach { session ->
                    SessionRow(session)
                }
            }
        }
        MetaText("Sayaç arka planda çalışır. Bildirim izni verirsen süreyi kilit ekranında görürsün.")
    }
}

@Composable
private fun TimerCard(
    timer: StudyTimerState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
) {
    val colors = LocalTuyoColors.current
    val hasRun = timer.running || timer.elapsedSeconds > 0
    TuyoCard(highlighted = timer.running) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(timer.mode.label)
            Spacer(Modifier.weight(1f))
            timer.subject?.let { Tag(text = it.label, color = subjectTint(it)) }
        }
        Spacer(Modifier.height(Space.md))
        Text(
            text = if (timer.targetSeconds > 0) {
                formatDuration(timer.remainingSeconds)
            } else {
                formatDuration(timer.elapsedSeconds)
            },
            style = NumericStyle,
            color = colors.textPrimary,
        )
        if (timer.targetSeconds > 0) {
            Spacer(Modifier.height(Space.sm))
            ProgressLine(fraction = timer.progress)
        }
        if (timer.finished) {
            Spacer(Modifier.height(Space.sm))
            Tag(text = "Süre tamamlandı", color = colors.success)
        }
        Spacer(Modifier.height(Space.md))
        if (hasRun) {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                if (timer.running) {
                    TuyoButton(
                        text = "Duraklat",
                        onClick = onPause,
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Secondary,
                    )
                } else if (!timer.finished) {
                    TuyoButton(
                        text = "Devam et",
                        onClick = onResume,
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Secondary,
                    )
                }
                if (timer.finished) {
                    TuyoButton(
                        text = "Yeni sayaç",
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Ghost,
                    )
                } else if (!timer.running) {
                    TuyoButton(
                        text = "Bitir ve kaydet",
                        onClick = {
                            onStop()
                            onReset()
                        },
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Ghost,
                    )
                }
                if (timer.running) {
                    TuyoButton(
                        text = "Bitir ve kaydet",
                        onClick = {
                            onStop()
                            onReset()
                        },
                        modifier = Modifier.weight(1f),
                        variant = TuyoButtonVariant.Ghost,
                    )
                }
            }
        } else {
            MetaText("Pomodoro, serbest çalışma veya sınav simülasyonu başlat.")
        }
    }
}

@Composable
private fun SessionRow(session: StudySession) {
    val colors = LocalTuyoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.subject?.label ?: "Genel",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
            )
            MetaText(
                session.date.format(DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("tr"))) +
                    " · ${session.mode.label}",
            )
        }
        Text(
            text = "${session.minutes} dk",
            style = MaterialTheme.typography.labelMedium,
            color = colors.accent,
        )
    }
}

@Composable
private fun ExamsContent(
    data: StudyData,
    onAddExam: () -> Unit,
    onAnalyzeExam: (ExamNet) -> Unit,
    onAddNetGoal: () -> Unit,
    onDeleteNetGoal: (String) -> Unit,
    onStartSimulation: (Int) -> Unit,
) {
    val colors = LocalTuyoColors.current
    val user = remember(data) { data.asUserData() }
    val exams = data.nets.sortedByDescending { it.date }

    Column(verticalArrangement = Arrangement.spacedBy(Space.lg)) {
        SectionHeader(
            title = "Net hedefleri",
            actionLabel = "Hedef ekle",
            onAction = onAddNetGoal,
        )
        if (data.netGoals.isEmpty()) {
            TuyoCard {
                MetaText("Bu ay için bir net hedefi koy; en yeni denemeyle otomatik ölçülür.")
            }
        } else {
            TuyoCard {
                data.netGoals.forEachIndexed { index, goal ->
                    NetGoalRow(
                        goal = goal,
                        current = user.currentNetFor(goal),
                        onDelete = { onDeleteNetGoal(goal.id) },
                    )
                    if (index != data.netGoals.lastIndex) {
                        Spacer(Modifier.height(Space.sm))
                        Hairline()
                        Spacer(Modifier.height(Space.sm))
                    }
                }
            }
        }

        SectionHeader(title = "Sınav simülasyonu")
        TuyoCard {
            Text(
                text = "Gerçek süreyle bir deneme çöz. Süre bitince sayaç otomatik kapanır.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(Space.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                TuyoButton(
                    text = "TYT · 165 dk",
                    onClick = { onStartSimulation(165) },
                    modifier = Modifier.weight(1f),
                    variant = TuyoButtonVariant.Secondary,
                )
                TuyoButton(
                    text = "AYT · 180 dk",
                    onClick = { onStartSimulation(180) },
                    modifier = Modifier.weight(1f),
                    variant = TuyoButtonVariant.Secondary,
                )
            }
        }

        SectionHeader(
            title = "Denemeler",
            actionLabel = "Ders bazlı ekle",
            onAction = onAddExam,
        )
        if (exams.isEmpty()) {
            TuyoCard { MetaText("Henüz deneme yok. İlk denemeyi eklediğinde analiz kartı açılır.") }
        } else {
            exams.take(8).forEach { exam ->
                ExamCard(exam = exam, onAnalyze = { onAnalyzeExam(exam) })
            }
        }
        Text(
            text = "Tahmini puan 100–500 aralığında bir yol göstericidir; resmî ÖSYM puanı değildir.",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun NetGoalRow(goal: NetGoal, current: Double?, onDelete: () -> Unit) {
    val colors = LocalTuyoColors.current
    val fraction = if (current != null) (current / goal.targetNet).toFloat() else 0f
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${goal.exam.label} · ${goal.subject?.label ?: "Toplam"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary,
                )
                MetaText(
                    if (current != null) {
                        "${netText(current)} / ${netText(goal.targetNet)} net"
                    } else {
                        "Bu ay kayıt yok · hedef ${netText(goal.targetNet)}"
                    },
                )
            }
            TuyoTextAction(
                text = "×",
                onClick = onDelete,
                color = colors.textTertiary,
                trailingArrow = false,
            )
        }
        Spacer(Modifier.height(4.dp))
        ProgressLine(fraction = fraction, color = colors.accent)
    }
}

@Composable
private fun ExamCard(exam: ExamNet, onAnalyze: () -> Unit) {
    val colors = LocalTuyoColors.current
    TuyoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Tag(text = exam.exam.label, color = colors.accent)
                    Spacer(Modifier.width(Space.sm))
                    MetaText(
                        exam.date.format(
                            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr")),
                        ),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${netText(exam.calculatedNet)} net",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                )
                MetaText("~${exam.estimatedScore} puan · ${exam.durationMinutes} dk")
            }
            if (exam.isDetailed) {
                TuyoButton(
                    text = if (exam.analysisCompleted) "Analiz" else "Analiz et",
                    onClick = onAnalyze,
                    variant = TuyoButtonVariant.Secondary,
                )
            }
        }
        if (exam.isDetailed) {
            Spacer(Modifier.height(Space.md))
            val sections = exam.subjectSections().sortedByDescending { it.loss }.take(4)
            sections.forEach { section ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = section.subject.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    MetaText("${section.correct}/${section.wrong}/${section.blank}")
                    Spacer(Modifier.width(Space.sm))
                    Text(
                        text = "${netText(section.net)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (section.net < 0) colors.danger else colors.textPrimary,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End,
                    )
                }
            }
            if (!exam.analysisCompleted) {
                Spacer(Modifier.height(Space.sm))
                Tag(text = "Analiz bekliyor", color = colors.warning)
            } else if (exam.analysisNote.isNotBlank() || exam.lossTopics.isNotBlank()) {
                Spacer(Modifier.height(Space.sm))
                if (exam.lossTopics.isNotBlank()) {
                    Text(
                        text = "Kayıp konular: ${exam.lossTopics}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                    )
                }
                if (exam.analysisNote.isNotBlank()) {
                    Text(
                        text = exam.analysisNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
            }
        } else {
            Spacer(Modifier.height(Space.sm))
            MetaText("Hızlı net kaydı · ders kırılımı yok")
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
