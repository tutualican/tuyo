package io.github.tutualican.tuyo.domain

import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.UserData
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * A topic is only "weak" when the student's own notebook says so. There are no
 * fixture mastery values in the app.
 */
data class TopicStat(
    val topic: Topic,
    val mistakeCount: Int,
    val dueCount: Int,
    val mastery: Int?,
    val lastLoggedOn: LocalDate?,
) {
    val hasUserData: Boolean get() = mistakeCount > 0
    val isWeak: Boolean get() = mastery != null && (dueCount > 0 || mastery <= 2)
    val masteryFraction: Float get() = (mastery ?: 0).coerceIn(0, 5) / 5f
}

fun List<Mistake>.topicStats(
    topics: List<Topic>,
    today: LocalDate = LocalDate.now(),
): List<TopicStat> =
    topics.map { topic ->
        val items = filter { it.topicId == topic.id }
        TopicStat(
            topic = topic,
            mistakeCount = items.size,
            dueCount = items.count { it.isDue(today) },
            mastery = items.takeIf { it.isNotEmpty() }
                ?.map { it.mastery }
                ?.average()
                ?.roundToInt(),
            lastLoggedOn = items.maxOfOrNull { it.createdOn },
        )
    }

/**
 * A streak is a record of real activity, never a decorative number. Missing a
 * day resets the count to one; logging twice today does not inflate it.
 */
fun nextStreak(
    current: Int,
    lastActivityEpochDay: Long?,
    today: LocalDate,
): Int {
    val todayEpoch = today.toEpochDay()
    return when (lastActivityEpochDay) {
        null -> 1
        todayEpoch -> current.coerceAtLeast(1)
        todayEpoch - 1 -> current + 1
        else -> 1
    }
}

/**
 * The stored count is only a streak while the last activity is today or
 * yesterday. After a missed day the UI must show zero, not a stale badge.
 */
fun UserData.currentStreak(today: LocalDate): Int {
    val last = lastActivityEpochDay ?: return 0
    val gap = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(last), today)
    return if (gap in 0L..1L) streakDays.coerceAtLeast(1) else 0
}

fun UserData.withActivity(today: LocalDate): UserData = copy(
    streakDays = nextStreak(streakDays, lastActivityEpochDay, today),
    lastActivityEpochDay = today.toEpochDay(),
)
