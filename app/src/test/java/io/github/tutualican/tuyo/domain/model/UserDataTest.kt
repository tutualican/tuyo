package io.github.tutualican.tuyo.domain.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UserDataTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private fun mistake(
        id: String = "m1",
        mastery: Int = 0,
        dueOn: LocalDate = LocalDate.of(2026, 1, 2),
    ) = Mistake(
        id = id,
        subject = Subject.MATEMATIK,
        topicId = "turev",
        topicTitle = "Türev",
        questionRef = "Deneme 3 · Soru 12",
        note = "Zincir kuralında iç türevi atladım.",
        errorKind = ErrorKind.ISLEM,
        createdOn = LocalDate.of(2026, 1, 1),
        dueOn = dueOn,
        mastery = mastery,
    )

    private fun userData() = UserData(
        profile = UserProfile(
            name = "Ada",
            track = ExamTrack.SAYISAL,
            onboardingComplete = true,
            reminderEnabled = true,
            reminderHour = 20,
            reminderMinute = 30,
        ),
        mistakes = listOf(mistake()),
        nets = listOf(
            ExamNet(
                id = "n1",
                date = LocalDate.of(2026, 1, 3),
                exam = ExamType.TYT,
                net = 62.5,
            ),
        ),
        streakDays = 4,
        lastActivityEpochDay = LocalDate.of(2026, 1, 3).toEpochDay(),
        reviewsCompleted = 2,
    )

    @Test
    fun `a user document survives a JSON round trip`() {
        val original = userData()
        val raw = json.encodeToString(UserData.serializer(), original)
        val decoded = json.decodeFromString(UserData.serializer(), raw)

        assertEquals(original, decoded)
        assertEquals("Ada", decoded.profile.name)
        assertEquals(ExamTrack.SAYISAL, decoded.profile.track)
        assertEquals(1, decoded.mistakes.size)
        assertEquals(62.5, decoded.nets.single().net, 0.0)
    }

    @Test
    fun `local dates are stored as readable ISO strings`() {
        val raw = json.encodeToString(UserData.serializer(), userData())

        assertTrue(raw.contains("\"2026-01-01\""))
        assertTrue(raw.contains("\"2026-01-03\""))
    }

    @Test
    fun `due mistakes are ordered by the earliest review date`() {
        val today = LocalDate.now()
        val data = UserData(
            mistakes = listOf(
                mistake(id = "newer", dueOn = today.minusDays(1)),
                mistake(id = "older", dueOn = today.minusDays(3)),
            ),
        )

        assertEquals(listOf("older", "newer"), data.dueMistakes.map { it.id })
    }

    @Test
    fun `an empty document has no invented learner data`() {
        val empty = UserData()

        assertEquals("", empty.profile.name)
        assertEquals(null, empty.profile.track)
        assertEquals(0, empty.streakDays)
        assertEquals(0, empty.mistakes.size)
        assertEquals(0, empty.nets.size)
        assertEquals(0, empty.reviewsCompleted)
    }
}
