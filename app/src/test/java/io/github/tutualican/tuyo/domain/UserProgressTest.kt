package io.github.tutualican.tuyo.domain

import io.github.tutualican.tuyo.domain.model.ErrorKind
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.UserData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UserProgressTest {

    private val today = LocalDate.of(2026, 3, 10)

    private fun mistake(
        id: String,
        topicId: String,
        mastery: Int,
        dueOn: LocalDate,
    ) = Mistake(
        id = id,
        subject = Subject.MATEMATIK,
        topicId = topicId,
        topicTitle = "Konu",
        questionRef = "Elle eklendi",
        note = "Not",
        errorKind = ErrorKind.BILGI,
        createdOn = today.minusDays(2),
        dueOn = dueOn,
        mastery = mastery,
    )

    private fun topic(id: String, title: String) = Topic(
        id = id,
        subject = Subject.MATEMATIK,
        title = title,
        summary = "Özet",
        keyIdea = "Fikir",
        confusions = emptyList(),
    )

    @Test
    fun `first activity starts a one day streak`() {
        assertEquals(1, nextStreak(current = 0, lastActivityEpochDay = null, today = today))
    }

    @Test
    fun `consecutive day increases the streak`() {
        assertEquals(
            4,
            nextStreak(
                current = 3,
                lastActivityEpochDay = today.minusDays(1).toEpochDay(),
                today = today,
            ),
        )
    }

    @Test
    fun `the same day does not inflate the streak`() {
        assertEquals(
            3,
            nextStreak(
                current = 3,
                lastActivityEpochDay = today.toEpochDay(),
                today = today,
            ),
        )
    }

    @Test
    fun `a missed day resets the streak`() {
        assertEquals(
            1,
            nextStreak(
                current = 9,
                lastActivityEpochDay = today.minusDays(2).toEpochDay(),
                today = today,
            ),
        )
    }

    @Test
    fun `a stale streak is shown as zero`() {
        val stale = UserData(
            streakDays = 12,
            lastActivityEpochDay = today.minusDays(3).toEpochDay(),
        )

        assertEquals(0, stale.currentStreak(today))
    }

    @Test
    fun `yesterdays streak is still alive today`() {
        val alive = UserData(
            streakDays = 12,
            lastActivityEpochDay = today.minusDays(1).toEpochDay(),
        )

        assertEquals(12, alive.currentStreak(today))
    }

    @Test
    fun `activity stores the day and updates the streak`() {
        val before = UserData(
            streakDays = 2,
            lastActivityEpochDay = today.minusDays(1).toEpochDay(),
        )

        val after = before.withActivity(today)

        assertEquals(3, after.streakDays)
        assertEquals(today.toEpochDay(), after.lastActivityEpochDay)
    }

    @Test
    fun `topic stats are derived only from logged mistakes`() {
        val topics = listOf(topic("turev", "Türev"), topic("limit", "Limit"))
        val mistakes = listOf(
            mistake("a", "turev", mastery = 1, dueOn = today),
            mistake("b", "turev", mastery = 3, dueOn = today.plusDays(3)),
        )

        val stats = mistakes.topicStats(topics, today)

        val turev = stats.first { it.topic.id == "turev" }
        val limit = stats.first { it.topic.id == "limit" }

        assertEquals(2, turev.mistakeCount)
        assertEquals(1, turev.dueCount)
        assertEquals(2, turev.mastery)
        assertTrue(turev.hasUserData)
        assertTrue(turev.isWeak)

        assertEquals(0, limit.mistakeCount)
        assertNull(limit.mastery)
        assertFalse(limit.hasUserData)
        assertFalse(limit.isWeak)
    }

    @Test
    fun `a fully reviewed topic is not marked weak`() {
        val topics = listOf(topic("turev", "Türev"))
        val mistakes = listOf(
            mistake("a", "turev", mastery = 4, dueOn = today.plusDays(7)),
        )

        val stat = mistakes.topicStats(topics, today).single()

        assertEquals(4, stat.mastery)
        assertEquals(0, stat.dueCount)
        assertFalse(stat.isWeak)
    }
}
