package io.github.tutualican.tuyo.domain

import java.time.LocalDate

/**
 * A small, explicit spaced-repetition ladder for re-solving wrong questions.
 *
 * This is intentionally not a general-purpose SRS. The first four steps are
 * 1/3/7/21 days; the later steps keep the ladder useful for long preparation.
 * A predictable ladder is easier for a student to trust than an opaque
 * algorithm. FSRS can replace this behind the same function later.
 */
object ReviewSchedule {

    /** Days until the next re-solve for mastery 0..5. */
    val intervals: List<Long> = listOf(1, 3, 7, 21, 45, 90)

    const val MAX_MASTERY = 5

    fun plan(
        mastery: Int,
        outcome: ReviewOutcome,
        today: LocalDate,
    ): ReviewPlan {
        val current = mastery.coerceIn(0, MAX_MASTERY)
        val next = when (outcome) {
            ReviewOutcome.AGAIN -> 0
            ReviewOutcome.HARD -> current
            ReviewOutcome.GOOD -> (current + 1).coerceAtMost(MAX_MASTERY)
            ReviewOutcome.EASY -> (current + 2).coerceAtMost(MAX_MASTERY)
        }
        val days = if (outcome == ReviewOutcome.AGAIN) 1L else intervals[next]
        return ReviewPlan(mastery = next, dueOn = today.plusDays(days))
    }
}

enum class ReviewOutcome { AGAIN, HARD, GOOD, EASY }

data class ReviewPlan(
    val mastery: Int,
    val dueOn: LocalDate,
)
