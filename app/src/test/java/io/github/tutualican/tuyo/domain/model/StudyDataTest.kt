package io.github.tutualican.tuyo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StudyDataTest {

    private val today = LocalDate.of(2026, 10, 22)

    private fun mistake(
        id: String,
        mastery: Int = 1,
        dueOn: LocalDate = today,
    ) = Mistake(
        id = id,
        subject = Subject.MATEMATIK,
        topicId = "turev",
        topicTitle = "Türev",
        questionRef = "2025 AYT",
        note = "Zincir kuralını atladım.",
        errorKind = ErrorKind.ISLEM,
        createdOn = today.minusDays(3),
        dueOn = dueOn,
        mastery = mastery,
    )

    private fun data(vararg mistakes: Mistake) = StudyData(
        learnerName = "Deniz",
        streakDays = 4,
        nets = emptyList(),
        mistakes = mistakes.toList(),
        topics = emptyList(),
        tactics = emptyList(),
        featuredTacticId = "",
    )

    @Test
    fun `withReview updates only the target mistake`() {
        val first = mistake("a", mastery = 1, dueOn = today)
        val second = mistake("b", mastery = 2, dueOn = today.plusDays(3))

        val updated = data(first, second).withReview(
            mistakeId = "a",
            mastery = 3,
            dueOn = today.plusDays(14),
        )

        assertEquals(3, updated.mistakes[0].mastery)
        assertEquals(today.plusDays(14), updated.mistakes[0].dueOn)
        assertEquals(second, updated.mistakes[1])
    }

    @Test
    fun `withReview ignores an unknown mistake id`() {
        val original = data(mistake("a"))

        val updated = original.withReview(
            mistakeId = "missing",
            mastery = 4,
            dueOn = today.plusDays(30),
        )

        assertEquals(original, updated)
    }

    @Test
    fun `the Y K S timeline derives from mastery`() {
        assertFalse(mistake("a", mastery = 0).topicStepDone)
        assertTrue(mistake("a", mastery = 1).topicStepDone)
        assertFalse(mistake("a", mastery = 2).questionStepDone)
        assertTrue(mistake("a", mastery = 3).questionStepDone)
    }

    @Test
    fun `a mistake is due on or after its due date`() {
        assertFalse(mistake("a", dueOn = today.plusDays(1)).isDue(today))
        assertTrue(mistake("a", dueOn = today).isDue(today))
        assertTrue(mistake("a", dueOn = today.minusDays(1)).isDue(today))
    }
}
