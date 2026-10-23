package io.github.tutualican.tuyo.domain

import io.github.tutualican.tuyo.domain.model.ExamNet
import io.github.tutualican.tuyo.domain.model.ExamSectionResult
import io.github.tutualican.tuyo.domain.model.ExamTrack
import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.NetGoal
import io.github.tutualican.tuyo.domain.model.PlanMetric
import io.github.tutualican.tuyo.domain.model.PlanPeriod
import io.github.tutualican.tuyo.domain.model.PlanTask
import io.github.tutualican.tuyo.domain.model.QuestionLog
import io.github.tutualican.tuyo.domain.model.StudySession
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.TopicProgress
import io.github.tutualican.tuyo.domain.model.TopicStatus
import io.github.tutualican.tuyo.domain.model.UserData
import io.github.tutualican.tuyo.domain.model.TimerMode
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyMetricsTest {

    private val monday = LocalDate.of(2026, 10, 19)
    private val wednesday = LocalDate.of(2026, 10, 21)

    private fun log(date: LocalDate, subject: Subject, correct: Int, wrong: Int, blank: Int = 0) =
        QuestionLog("log-$date-$subject", date, subject, correct, wrong, blank)

    private fun task(
        id: String,
        date: LocalDate,
        period: PlanPeriod = PlanPeriod.DAILY,
        completed: Boolean = false,
    ) = PlanTask(
        id = id,
        title = id,
        period = period,
        metric = PlanMetric.QUESTIONS,
        target = 20,
        completed = completed,
        date = date,
    )

    @Test
    fun weekStartsOnMonday() {
        assertEquals(monday, UserData().weekStart(wednesday))
        assertEquals(monday, UserData().weekStart(monday))
    }

    @Test
    fun weeklySummaryOnlyCountsCurrentWeek() {
        val user = UserData(
            questionLogs = listOf(
                log(monday, Subject.MATEMATIK, 10, 2),
                log(wednesday, Subject.TURKCE, 8, 0, 2),
                log(monday.minusDays(1), Subject.FIZIK, 50, 0),
            ),
            studySessions = listOf(
                StudySession("s1", wednesday, Subject.MATEMATIK, 25, TimerMode.POMODORO),
                StudySession("s2", monday.minusDays(7), Subject.FIZIK, 60),
            ),
            nets = listOf(
                ExamNet("e1", wednesday, ExamType.TYT, 80.0),
                ExamNet("e2", monday.minusDays(7), ExamType.TYT, 90.0),
            ),
            reviewsCompleted = 9,
            reviewEpochDays = listOf(
                monday.toEpochDay(),
                wednesday.toEpochDay(),
                monday.minusDays(8).toEpochDay(),
            ),
        )

        val summary = user.weeklySummary(wednesday)

        assertEquals(monday, summary.start)
        assertEquals(LocalDate.of(2026, 10, 25), summary.end)
        assertEquals(22, summary.questions)
        assertEquals(18, summary.correct)
        assertEquals(2, summary.wrong)
        assertEquals(2, summary.blank)
        assertEquals(25, summary.minutes)
        assertEquals(1, summary.exams)
        assertEquals(2, summary.reviews)
        assertEquals(17.5, summary.net, 0.001)
        assertEquals(7, summary.days.size)
    }

    @Test
    fun dailyAndWeeklyTasksAreSeparatedAndSorted() {
        val user = UserData(
            planTasks = listOf(
                task("done", wednesday, completed = true),
                task("open", wednesday),
                task("old", wednesday.minusDays(1)),
                task("week", monday, period = PlanPeriod.WEEKLY),
            ),
        )

        val daily = user.planTasksOn(wednesday)
        assertEquals(listOf("open", "done"), daily.map { it.id })
        assertEquals(listOf("week"), user.weeklyPlanTasks(wednesday).map { it.id })
        assertEquals(listOf("old"), user.overduePlanTasks(wednesday).map { it.id })
    }

    @Test
    fun currentNetUsesLatestMatchingMonthAndSubject() {
        val goal = NetGoal(
            id = "g1",
            exam = ExamType.AYT,
            subject = Subject.MATEMATIK,
            targetNet = 30.0,
            year = 2026,
            month = 10,
        )
        val user = UserData(
            nets = listOf(
                ExamNet(
                    id = "old",
                    date = LocalDate.of(2026, 10, 1),
                    exam = ExamType.AYT,
                    net = 10.0,
                    sections = listOf(ExamSectionResult(Subject.MATEMATIK, correct = 10)),
                ),
                ExamNet(
                    id = "new",
                    date = LocalDate.of(2026, 10, 20),
                    exam = ExamType.AYT,
                    net = 20.0,
                    sections = listOf(ExamSectionResult(Subject.MATEMATIK, correct = 20)),
                ),
                ExamNet("previous-month", LocalDate.of(2026, 9, 30), ExamType.AYT, 50.0),
            ),
        )

        assertEquals(20.0, user.currentNetFor(goal)!!, 0.001)
        assertEquals(20.0, user.currentNetFor(goal.copy(subject = null))!!, 0.001)

        val missing = goal.copy(subject = Subject.FIZIK)
        assertNull(user.currentNetFor(missing))
    }

    @Test
    fun planSuggestionsExplainWeakTopicAndMissingSession() {
        val topic = Topic(
            id = "t1",
            subject = Subject.MATEMATIK,
            title = "Turev",
            summary = "",
            keyIdea = "",
            confusions = emptyList(),
        )
        val mistake = Mistake(
            id = "m1",
            subject = Subject.MATEMATIK,
            topicId = topic.id,
            topicTitle = topic.title,
            questionRef = "",
            note = "",
            errorKind = io.github.tutualican.tuyo.domain.model.ErrorKind.BILGI,
            createdOn = monday,
            dueOn = monday,
            mastery = 1,
        )
        val user = UserData(
            mistakes = listOf(mistake),
            topicProgress = listOf(
                TopicProgress(
                    topicId = topic.id,
                    status = TopicStatus.NEEDS_REVIEW,
                    updatedOn = monday,
                ),
            ),
        )

        val suggestions = user.planSuggestions(listOf(topic), wednesday)

        assertTrue(suggestions.any { it.id == "due-review" })
        assertTrue(suggestions.any { it.id == "weak-t1" })
        assertTrue(suggestions.any { it.id == "focus-block" })
    }

    @Test
    fun topicStatusMarksWeakEvenWithoutMistakes() {
        val topic = Topic("t1", Subject.FIZIK, "Kuvvet", "", "", emptyList())
        val stats = emptyList<Mistake>().topicStats(
            topics = listOf(topic),
            today = wednesday,
            progress = listOf(
                TopicProgress(topic.id, TopicStatus.NEEDS_REVIEW, wednesday),
            ),
        )

        val stat = stats.single()
        assertEquals(TopicStatus.NEEDS_REVIEW, stat.status)
        assertTrue(stat.isWeak)
        assertTrue(stat.hasUserData)
    }

    @Test
    fun detailedExamUsesSectionNetAndTransparentScore() {
        val exam = ExamNet(
            id = "e1",
            date = wednesday,
            exam = ExamType.TYT,
            net = 0.0,
            sections = listOf(
                ExamSectionResult(Subject.TURKCE, correct = 30, wrong = 10),
                ExamSectionResult(Subject.MATEMATIK, correct = 20, wrong = 0),
            ),
        )

        assertEquals(47.5, exam.calculatedNet, 0.001)
        assertTrue(exam.isDetailed)
        assertEquals(27.5, exam.sectionNet(Subject.TURKCE)!!, 0.001)
        assertEquals(258, exam.estimatedScore)
        assertEquals(100, exam.copy(sections = listOf(ExamSectionResult(Subject.TURKCE, wrong = 40))).estimatedScore)
        assertEquals(500, exam.copy(net = 120.0, sections = emptyList()).estimatedScore)
    }

    @Test
    fun examTemplatesMatchOfficialQuestionCounts() {
        val tracks = ExamTrack.entries + null
        tracks.forEach { track ->
            assertEquals(80, sectionTemplates(ExamType.AYT, track).sumOf { it.questions })
        }
        assertEquals(80, sectionTemplates(ExamType.YDT, null).sumOf { it.questions })
        assertEquals(120, sectionTemplates(ExamType.TYT, ExamTrack.SOZEL).sumOf { it.questions })
    }
}
