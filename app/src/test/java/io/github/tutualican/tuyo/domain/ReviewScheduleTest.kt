package io.github.tutualican.tuyo.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReviewScheduleTest {

    private val today = LocalDate.of(2026, 10, 22)

    @Test
    fun `again resets mastery and returns tomorrow`() {
        val plan = ReviewSchedule.plan(
            mastery = 4,
            outcome = ReviewOutcome.AGAIN,
            today = today,
        )

        assertEquals(0, plan.mastery)
        assertEquals(today.plusDays(1), plan.dueOn)
    }

    @Test
    fun `good advances one step and schedules the matching interval`() {
        val plan = ReviewSchedule.plan(
            mastery = 1,
            outcome = ReviewOutcome.GOOD,
            today = today,
        )

        assertEquals(2, plan.mastery)
        assertEquals(today.plusDays(ReviewSchedule.intervals[2]), plan.dueOn)
    }

    @Test
    fun `easy advances two steps`() {
        val plan = ReviewSchedule.plan(
            mastery = 0,
            outcome = ReviewOutcome.EASY,
            today = today,
        )

        assertEquals(2, plan.mastery)
        assertEquals(today.plusDays(7), plan.dueOn)
    }

    @Test
    fun `hard keeps mastery but still schedules a future review`() {
        val plan = ReviewSchedule.plan(
            mastery = 3,
            outcome = ReviewOutcome.HARD,
            today = today,
        )

        assertEquals(3, plan.mastery)
        assertTrue(plan.dueOn.isAfter(today))
    }

    @Test
    fun `mastery never exceeds the cap`() {
        val plan = ReviewSchedule.plan(
            mastery = 5,
            outcome = ReviewOutcome.EASY,
            today = today,
        )

        assertEquals(ReviewSchedule.MAX_MASTERY, plan.mastery)
        assertEquals(today.plusDays(ReviewSchedule.intervals.last()), plan.dueOn)
    }

    @Test
    fun `the ladder is strictly increasing`() {
        ReviewSchedule.intervals.zipWithNext().forEach { (first, second) ->
            assertTrue("$first should be shorter than $second", second > first)
        }
    }

    @Test
    fun `an unknown mastery is clamped instead of crashing`() {
        val plan = ReviewSchedule.plan(
            mastery = 99,
            outcome = ReviewOutcome.GOOD,
            today = today,
        )

        assertEquals(ReviewSchedule.MAX_MASTERY, plan.mastery)
    }
}
