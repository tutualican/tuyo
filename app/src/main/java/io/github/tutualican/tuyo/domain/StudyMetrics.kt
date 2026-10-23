package io.github.tutualican.tuyo.domain

import io.github.tutualican.tuyo.domain.model.NetGoal
import io.github.tutualican.tuyo.domain.model.PlanMetric
import io.github.tutualican.tuyo.domain.model.PlanPeriod
import io.github.tutualican.tuyo.domain.model.PlanTask
import io.github.tutualican.tuyo.domain.model.QuestionLog
import io.github.tutualican.tuyo.domain.model.StudySession
import io.github.tutualican.tuyo.domain.model.StudyData
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.TopicProgress
import io.github.tutualican.tuyo.domain.model.UserData
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class DayStudySummary(
    val date: LocalDate,
    val questions: Int,
    val minutes: Int,
    val net: Double,
)

data class WeeklySummary(
    val start: LocalDate,
    val days: List<DayStudySummary>,
    val questions: Int,
    val correct: Int,
    val wrong: Int,
    val blank: Int,
    val minutes: Int,
    val sessions: Int,
    val exams: Int,
    val reviews: Int,
) {
    val end: LocalDate get() = start.plusDays(6)
    val net: Double get() = correct - wrong / 4.0
}

data class PlanSuggestion(
    val id: String,
    val title: String,
    val reason: String,
    val subject: Subject?,
    val metric: PlanMetric,
    val target: Int,
)

fun StudyData.asUserData(): UserData = UserData(
    profile = profile,
    mistakes = mistakes,
    nets = nets,
    streakDays = streakDays,
    reviewsCompleted = reviewsCompleted,
    questionLogs = questionLogs,
    topicProgress = topicProgress,
    planTasks = planTasks,
    studySessions = studySessions,
    netGoals = netGoals,
    restDayEpochDays = restDayEpochDays,
    reviewEpochDays = reviewEpochDays,
)

fun UserData.weekStart(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun UserData.isRestDay(date: LocalDate): Boolean =
    date.toEpochDay() in restDayEpochDays

fun UserData.questionLogsOn(date: LocalDate): List<QuestionLog> =
    questionLogs.filter { it.date == date }

fun UserData.studySessionsOn(date: LocalDate): List<StudySession> =
    studySessions.filter { it.date == date }

fun UserData.studyMinutesOn(date: LocalDate): Int =
    studySessionsOn(date).sumOf { it.minutes }

fun UserData.questionCountOn(date: LocalDate): Int =
    questionLogsOn(date).sumOf { it.total }

fun UserData.planTasksOn(date: LocalDate): List<PlanTask> =
    planTasks.filter { it.period == PlanPeriod.DAILY && it.date == date }
        .sortedWith(compareBy({ it.completed }, { it.title }))

fun UserData.weeklyPlanTasks(date: LocalDate): List<PlanTask> {
    val start = weekStart(date)
    return planTasks.filter { it.period == PlanPeriod.WEEKLY && it.date == start }
        .sortedWith(compareBy({ it.completed }, { it.title }))
}

fun UserData.overduePlanTasks(date: LocalDate): List<PlanTask> =
    planTasks.filter {
        it.period == PlanPeriod.DAILY && !it.completed && it.date.isBefore(date)
    }

fun UserData.topicProgressFor(topicId: String): TopicProgress? =
    topicProgress.firstOrNull { it.topicId == topicId }

fun UserData.currentNetFor(goal: NetGoal): Double? {
    val inMonth = nets
        .filter {
            it.exam == goal.exam &&
                it.date.year == goal.year &&
                it.date.monthValue == goal.month
        }
        .sortedByDescending { it.date }
    val subject = goal.subject
    if (subject != null) {
        return inMonth.firstNotNullOfOrNull { it.sectionNet(subject) }
    }
    return inMonth.firstOrNull()?.calculatedNet
}

fun UserData.weeklySummary(today: LocalDate): WeeklySummary {
    val start = weekStart(today)
    val days = (0L..6L).map { offset ->
        val date = start.plusDays(offset)
        val logs = questionLogsOn(date)
        DayStudySummary(
            date = date,
            questions = logs.sumOf { it.total },
            minutes = studyMinutesOn(date),
            net = logs.sumOf { it.net },
        )
    }
    val logs = questionLogs.filter { !it.date.isBefore(start) && !it.date.isAfter(start.plusDays(6)) }
    val sessions = studySessions.filter {
        !it.date.isBefore(start) && !it.date.isAfter(start.plusDays(6))
    }
    return WeeklySummary(
        start = start,
        days = days,
        questions = logs.sumOf { it.total },
        correct = logs.sumOf { it.correct },
        wrong = logs.sumOf { it.wrong },
        blank = logs.sumOf { it.blank },
        minutes = sessions.sumOf { it.minutes },
        sessions = sessions.size,
        exams = nets.count { !it.date.isBefore(start) && !it.date.isAfter(start.plusDays(6)) },
        reviews = reviewEpochDays.count {
            val day = LocalDate.ofEpochDay(it)
            !day.isBefore(start) && !day.isAfter(start.plusDays(6))
        },
    )
}

/**
 * Transparent, rule-based suggestions. No model, no hidden score: every line
 * names the data that produced it.
 */
fun UserData.planSuggestions(
    topics: List<Topic>,
    today: LocalDate = LocalDate.now(),
): List<PlanSuggestion> {
    val stats = mistakes.topicStats(topics, today, topicProgress)
    val weak = stats
        .filter { it.isWeak }
        .sortedWith(compareByDescending<TopicStat> { it.dueCount }.thenBy { it.mastery ?: 99 })
        .firstOrNull()
    val due = mistakes.count { it.isDue(today) }
    val suggestions = mutableListOf<PlanSuggestion>()

    if (due > 0) {
        suggestions += PlanSuggestion(
            id = "due-review",
            title = "Yanlış tekrarı",
            reason = "Bugün $due yanlış tekrar bekliyor.",
            subject = null,
            metric = PlanMetric.QUESTIONS,
            target = due.coerceIn(1, 20),
        )
    }
    if (weak != null) {
        suggestions += PlanSuggestion(
            id = "weak-${weak.topic.id}",
            title = "${weak.topic.title} tekrarı",
            reason = if (weak.mistakeCount > 0) {
                "Defterinde ${weak.mistakeCount} kayıt var; en zayıf konu bu."
            } else {
                "Bu konuyu tekrar gerekli olarak işaretledin."
            },
            subject = weak.topic.subject,
            metric = PlanMetric.QUESTIONS,
            target = 20,
        )
    }
    val goal = netGoals.firstOrNull()
    if (goal != null) {
        val current = currentNetFor(goal)
        if (current == null || current < goal.targetNet) {
            suggestions += PlanSuggestion(
                id = "net-goal-${goal.id}",
                title = "Net hedefi için soru",
                reason = if (current == null) {
                    "Bu ay için henüz deneme yok; hedefi ölçmek için bir deneme ekle."
                } else {
                    "Hedefe ${(goal.targetNet - current).coerceAtLeast(0.1).formatNet()} net kaldı."
                },
                subject = goal.subject,
                metric = PlanMetric.QUESTIONS,
                target = 20,
            )
        }
    }
    if (studySessionsOn(today).isEmpty()) {
        suggestions += PlanSuggestion(
            id = "focus-block",
            title = "25 dakikalık odak",
            reason = "Bugün henüz çalışma kaydı yok. Küçük bir blok başlat.",
            subject = null,
            metric = PlanMetric.MINUTES,
            target = 25,
        )
    }
    return suggestions.take(3)
}

private fun Double.formatNet(): String =
    String.format(java.util.Locale.forLanguageTag("tr"), "%.1f", this)
