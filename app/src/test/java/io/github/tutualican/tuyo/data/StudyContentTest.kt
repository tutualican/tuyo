package io.github.tutualican.tuyo.data

import io.github.tutualican.tuyo.domain.model.ExamNet
import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.MotivationTheme
import io.github.tutualican.tuyo.domain.model.UserData
import io.github.tutualican.tuyo.domain.toTopicWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StudyContentTest {

    @Test
    fun `topic ids are unique and every tactic points at a real topic`() {
        val topics = StudyContent.topics
        val topicIds = topics.map { it.id }.toSet()

        assertEquals(topics.size, topicIds.size)
        StudyContent.tactics.forEach { tactic ->
            tactic.topicIds.forEach { topicId ->
                assertTrue(
                    "Tactic ${tactic.id} references unknown topic $topicId",
                    topicId in topicIds,
                )
            }
        }
    }

    @Test
    fun `topic frequency data is complete and internally consistent`() {
        assertEquals(StudyContent.topics.size, StudyContent.topics.count { it.frequency != null })

        StudyContent.topics.forEach { topic ->
            val frequency = requireNotNull(topic.frequency) {
                "Topic ${topic.id} needs a frequency reference"
            }
            assertTrue("Topic ${topic.id} needs at least five years", frequency.years.size >= 5)
            assertEquals(frequency.years.size, frequency.counts.size)
            assertEquals(frequency.years.size, frequency.totals.size)
            assertTrue(
                "Topic ${topic.id} years must be ascending",
                frequency.years.zipWithNext().all { (a, b) -> a < b },
            )
            assertTrue(
                "Topic ${topic.id} needs positive subject totals",
                frequency.totals.all { it > 0 },
            )
            frequency.counts.forEachIndexed { index, count ->
                assertTrue("Topic ${topic.id} has a negative count", count >= 0)
                assertTrue(
                    "Topic ${topic.id} has more questions than its subject in ${frequency.years[index]}",
                    count <= frequency.totals[index],
                )
            }
            assertTrue(
                "Topic ${topic.id} rank must fit its subject",
                frequency.rank in 1..frequency.rankedTopics,
            )

            val weight = frequency.toTopicWeight()
            assertTrue(weight.expectedLow <= weight.expectedHigh)
            assertTrue(weight.sharePercent in 0.0..100.0)
        }
    }

    @Test
    fun `tactic, note, quote and TED ids are unique`() {
        val tactics = StudyContent.tactics
        val motivation = StudyContent.motivation
        val quotes = StudyContent.quotes
        val tedTalks = StudyContent.tedTalks

        assertEquals(tactics.size, tactics.map { it.id }.toSet().size)
        assertEquals(motivation.size, motivation.map { it.id }.toSet().size)
        assertEquals(quotes.size, quotes.map { it.id }.toSet().size)
        assertEquals(tedTalks.size, tedTalks.map { it.id }.toSet().size)
        assertTrue(tactics.size >= 15)
        assertTrue(motivation.size >= 20)
    }

    @Test
    fun `motivation quotes and TED talks are complete and attributable`() {
        val quotes = StudyContent.quotes
        val tedTalks = StudyContent.tedTalks

        assertTrue(quotes.size >= 25)
        assertTrue(tedTalks.size >= 8)
        assertEquals(MotivationTheme.entries.toSet(), quotes.map { it.theme }.toSet())
        assertTrue(quotes.any { it.author == "Mustafa Kemal Atatürk" })

        quotes.forEach { quote ->
            assertTrue("Quote ${quote.id} has no text", quote.text.isNotBlank())
            assertTrue("Quote ${quote.id} has no author", quote.author.isNotBlank())
            assertTrue("Quote ${quote.id} has no source", quote.source.isNotBlank())
        }

        tedTalks.forEach { talk ->
            assertTrue("Talk ${talk.id} has no title", talk.title.isNotBlank())
            assertTrue("Talk ${talk.id} has no speaker", talk.speaker.isNotBlank())
            assertTrue("Talk ${talk.id} has no summary", talk.summary.isNotBlank())
            assertTrue(
                "Talk ${talk.id} must use an official TED https URL",
                talk.url.startsWith("https://www.ted.com/talks/"),
            )
        }

        assertTrue(StudyContent.motivationSources.size >= 3)
        StudyContent.motivationSources.forEach { source ->
            assertTrue(source.url.startsWith("https://"))
        }
    }

    @Test
    fun `the bundled exam data is complete enough to be useful`() {
        assertEquals(3, StudyContent.examOverviews.size)
        assertTrue(StudyContent.examStats.size >= 8)
        assertEquals(4, StudyContent.examComments.size)
        assertTrue(StudyContent.sources.size >= 5)
        StudyContent.sources.forEach { source ->
            assertTrue(source.url.startsWith("https://"))
        }
    }

    @Test
    fun `the built study data contains no hidden learner state`() {
        val data = StudyContent.studyData(UserData())

        assertEquals("", data.learnerName)
        assertEquals(null, data.profile.track)
        assertEquals(0, data.streakDays)
        assertEquals(0, data.reviewsCompleted)
        assertTrue(data.mistakes.isEmpty())
        assertTrue(data.nets.isEmpty())
    }

    @Test
    fun `a real user document is projected into the study view`() {
        val user = UserData(
            profile = io.github.tutualican.tuyo.domain.model.UserProfile(
                name = "Ada",
                track = io.github.tutualican.tuyo.domain.model.ExamTrack.SAYISAL,
                onboardingComplete = true,
            ),
            mistakes = listOf(
                Mistake(
                    id = "m1",
                    subject = io.github.tutualican.tuyo.domain.model.Subject.MATEMATIK,
                    topicId = "turev",
                    topicTitle = "Türev",
                    questionRef = "Elle eklendi",
                    note = "Zincir kuralı",
                    errorKind = io.github.tutualican.tuyo.domain.model.ErrorKind.BILGI,
                    createdOn = LocalDate.of(2026, 1, 1),
                    dueOn = LocalDate.of(2026, 1, 2),
                    mastery = 0,
                ),
            ),
        )

        val data = StudyContent.studyData(user)

        assertEquals("Ada", data.learnerName)
        assertEquals(1, data.mistakes.size)
        assertNotNull(data.tactics.firstOrNull { it.id == data.featuredTacticId })
        assertTrue(data.featuredTacticId.isNotBlank())
    }

    @Test
    fun `net trend never compares different exam types`() {
        val user = UserData(
            nets = listOf(
                ExamNet(
                    id = "1",
                    date = LocalDate.of(2026, 1, 1),
                    exam = ExamType.TYT,
                    net = 50.0,
                ),
                ExamNet(
                    id = "2",
                    date = LocalDate.of(2026, 1, 2),
                    exam = ExamType.AYT,
                    net = 30.0,
                ),
                ExamNet(
                    id = "3",
                    date = LocalDate.of(2026, 1, 3),
                    exam = ExamType.TYT,
                    net = 60.0,
                ),
            ),
        )

        val data = StudyContent.studyData(user)

        assertEquals(ExamType.TYT, data.latestNet?.exam)
        assertEquals(10.0, data.netDelta ?: 0.0, 0.0)
        assertEquals(listOf(50.0, 60.0), data.netTrend.map { it.net })
    }
}
