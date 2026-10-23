package io.github.tutualican.tuyo.domain

import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.TopicProgress
import io.github.tutualican.tuyo.domain.model.TopicStatus
import io.github.tutualican.tuyo.domain.model.UserData
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * A topic is only "weak" when the student's own notebook or explicit status
 * says so. There are no fixture mastery values in the app.
 */
data class TopicStat(
    val topic: Topic,
    val mistakeCount: Int,
    val dueCount: Int,
    val mastery: Int?,
    val lastLoggedOn: LocalDate?,
    val status: TopicStatus = TopicStatus.NOT_STARTED,
    val reviewStep: Int = 0,
    val nextReviewOn: LocalDate? = null,
) {
    val hasUserData: Boolean
        get() = mistakeCount > 0 || status != TopicStatus.NOT_STARTED
    val isWeak: Boolean
        get() = status == TopicStatus.NEEDS_REVIEW ||
            (mastery != null && (dueCount > 0 || mastery <= 2))
    val masteryFraction: Float get() = (mastery ?: 0).coerceIn(0, 5) / 5f
    val reviewDue: Boolean
        get() = nextReviewOn?.let { !it.isAfter(LocalDate.now()) } == true
}

fun List<Mistake>.topicStats(
    topics: List<Topic>,
    today: LocalDate = LocalDate.now(),
    progress: List<TopicProgress> = emptyList(),
): List<TopicStat> {
    val progressByTopic = progress.associateBy { it.topicId }
    return topics.map { topic ->
        val items = filter { it.topicId == topic.id }
        val topicProgress = progressByTopic[topic.id]
        TopicStat(
            topic = topic,
            mistakeCount = items.size,
            dueCount = items.count { it.isDue(today) },
            mastery = items.takeIf { it.isNotEmpty() }
                ?.map { it.mastery }
                ?.average()
                ?.roundToInt(),
            lastLoggedOn = listOfNotNull(
                items.maxOfOrNull { it.createdOn },
                topicProgress?.updatedOn,
            ).maxOrNull(),
            status = topicProgress?.status ?: TopicStatus.NOT_STARTED,
            reviewStep = topicProgress?.reviewStep ?: 0,
            nextReviewOn = topicProgress?.nextReviewOn,
        )
    }
}

/**
 * A streak is a record of real activity, never a decorative number. Missing a
 * day resets the count to one; logging twice today does not inflate it.
 */
fun nextStreak(
    current: Int,
    lastActivityEpochDay: Long?,
    today: LocalDate,
    restDayEpochDays: Set<Long> = emptySet(),
): Int {
    val todayEpoch = today.toEpochDay()
    return when (lastActivityEpochDay) {
        null -> 1
        todayEpoch -> current.coerceAtLeast(1)
        todayEpoch - 1 -> current + 1
        else -> {
            val last = LocalDate.ofEpochDay(lastActivityEpochDay)
            val missed = generateSequence(last.plusDays(1)) { it.plusDays(1) }
                .takeWhile { it.isBefore(today) }
                .toList()
            val onlyRestDays = missed.isNotEmpty() && missed.all {
                it.toEpochDay() in restDayEpochDays
            }
            if (onlyRestDays) current + 1 else 1
        }
    }
}

/**
 * The stored count is only a streak while every skipped day is a declared rest
 * day. After an unplanned missed day the UI shows zero, not a stale badge.
 */
fun UserData.currentStreak(today: LocalDate): Int {
    val last = lastActivityEpochDay ?: return 0
    val lastDate = LocalDate.ofEpochDay(last)
    val gap = ChronoUnit.DAYS.between(lastDate, today)
    if (gap !in 0L..1L) {
        val missed = generateSequence(lastDate.plusDays(1)) { it.plusDays(1) }
            .takeWhile { it.isBefore(today) }
            .toList()
        if (missed.isEmpty() || missed.any { it.toEpochDay() !in restDayEpochDays }) {
            return 0
        }
    }
    return streakDays.coerceAtLeast(1)
}

fun UserData.withActivity(today: LocalDate): UserData = copy(
    streakDays = nextStreak(
        current = streakDays,
        lastActivityEpochDay = lastActivityEpochDay,
        today = today,
        restDayEpochDays = restDayEpochDays.toSet(),
    ),
    lastActivityEpochDay = today.toEpochDay(),
)
