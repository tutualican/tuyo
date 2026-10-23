package io.github.tutualican.tuyo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.tutualican.tuyo.data.ExamAnalysisScheduler
import io.github.tutualican.tuyo.data.ReminderScheduler
import io.github.tutualican.tuyo.data.StudyTimer
import io.github.tutualican.tuyo.data.UserRepository
import io.github.tutualican.tuyo.domain.ReviewOutcome
import io.github.tutualican.tuyo.domain.ReviewSchedule
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
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.TimerMode
import io.github.tutualican.tuyo.domain.model.Topic
import io.github.tutualican.tuyo.domain.model.TopicProgress
import io.github.tutualican.tuyo.domain.model.TopicStatus
import io.github.tutualican.tuyo.domain.model.UserData
import io.github.tutualican.tuyo.domain.model.UserProfile
import io.github.tutualican.tuyo.domain.model.withReview
import io.github.tutualican.tuyo.domain.planSuggestions
import io.github.tutualican.tuyo.domain.topicProgressFor
import io.github.tutualican.tuyo.domain.withActivity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class TuyoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)
    private val reminders = ReminderScheduler

    /**
     * null means "not read yet"; the app shows a quiet loading state instead of
     * a flash of fake content. After the first emission, null never returns.
     */
    val user: StateFlow<UserData?> = repository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun completeOnboarding(
        name: String,
        track: ExamTrack,
        reminderEnabled: Boolean,
        reminderHour: Int,
        reminderMinute: Int,
    ) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    profile = current.profile.copy(
                        name = name.trim(),
                        track = track,
                        onboardingComplete = true,
                        reminderEnabled = reminderEnabled,
                        reminderHour = reminderHour.coerceIn(0, 23),
                        reminderMinute = reminderMinute.coerceIn(0, 59),
                    ),
                )
            }
            if (reminderEnabled) {
                reminders.schedule(getApplication(), reminderHour, reminderMinute)
            } else {
                reminders.cancel(getApplication())
            }
        }
    }

    fun addMistake(mistake: Mistake) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                current
                    .withActivity(today)
                    .copy(mistakes = listOf(mistake) + current.mistakes)
            }
        }
    }

    fun reviewMistake(mistakeId: String, outcome: ReviewOutcome) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                val mistake = current.mistakes.firstOrNull { it.id == mistakeId }
                    ?: return@update current
                val plan = ReviewSchedule.plan(mistake.mastery, outcome, today)
                current
                    .withReview(mistakeId, plan.mastery, plan.dueOn)
                    .withActivity(today)
                    .copy(
                        reviewsCompleted = current.reviewsCompleted + 1,
                        reviewEpochDays = current.reviewEpochDays + today.toEpochDay(),
                    )
            }
        }
    }

    fun addNet(exam: ExamType, net: Double) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val safeNet = net.coerceIn(0.0, exam.maxNet)
            repository.update { current ->
                current
                    .withActivity(today)
                    .copy(
                        nets = current.nets + ExamNet(
                            id = UUID.randomUUID().toString(),
                            date = today,
                            exam = exam,
                            net = safeNet,
                        ),
                    )
            }
        }
    }

    fun updateProfile(name: String, track: ExamTrack) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    profile = current.profile.copy(
                        name = name.trim().ifBlank { current.profile.name },
                        track = track,
                    ),
                )
            }
        }
    }

    fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    profile = current.profile.copy(
                        reminderEnabled = enabled,
                        reminderHour = hour.coerceIn(0, 23),
                        reminderMinute = minute.coerceIn(0, 59),
                    ),
                )
            }
            if (enabled) {
                reminders.schedule(getApplication(), hour, minute)
            } else {
                reminders.cancel(getApplication())
            }
        }
    }

    fun deleteMistake(mistakeId: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(mistakes = current.mistakes.filterNot { it.id == mistakeId })
            }
        }
    }

    fun addQuestionLog(
        date: LocalDate,
        subject: Subject,
        correct: Int,
        wrong: Int,
        blank: Int,
    ) {
        viewModelScope.launch {
            val activityDay = LocalDate.now()
            repository.update { current ->
                current.withActivity(activityDay).copy(
                    questionLogs = current.questionLogs + QuestionLog(
                        id = UUID.randomUUID().toString(),
                        date = date,
                        subject = subject,
                        correct = correct.coerceAtLeast(0),
                        wrong = wrong.coerceAtLeast(0),
                        blank = blank.coerceAtLeast(0),
                    ),
                )
            }
        }
    }

    fun deleteQuestionLog(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(questionLogs = current.questionLogs.filterNot { it.id == id })
            }
        }
    }

    fun setTopicStatus(topicId: String, status: TopicStatus) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                val existing = current.topicProgressFor(topicId)
                val next = when (status) {
                    TopicStatus.NOT_STARTED -> null
                    TopicStatus.IN_PROGRESS -> TopicProgress(
                        topicId = topicId,
                        status = status,
                        updatedOn = today,
                    )
                    TopicStatus.DONE -> TopicProgress(
                        topicId = topicId,
                        status = status,
                        updatedOn = today,
                        reviewStep = existing?.reviewStep ?: 0,
                        nextReviewOn = today.plusDays(1),
                    )
                    TopicStatus.NEEDS_REVIEW -> TopicProgress(
                        topicId = topicId,
                        status = status,
                        updatedOn = today,
                        reviewStep = existing?.reviewStep ?: 0,
                        nextReviewOn = today,
                    )
                }
                current.withActivity(today).copy(
                    topicProgress = current.topicProgress
                        .filterNot { it.topicId == topicId } + listOfNotNull(next),
                )
            }
        }
    }

    fun reviewTopic(topicId: String) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                val existing = current.topicProgressFor(topicId)
                val step = (existing?.reviewStep ?: -1) + 1
                val interval = ReviewSchedule.intervals.getOrElse(step) {
                    ReviewSchedule.intervals.last()
                }
                val next = TopicProgress(
                    topicId = topicId,
                    status = TopicStatus.DONE,
                    updatedOn = today,
                    reviewStep = step,
                    nextReviewOn = today.plusDays(interval),
                )
                current.withActivity(today).copy(
                    reviewsCompleted = current.reviewsCompleted + 1,
                    reviewEpochDays = current.reviewEpochDays + today.toEpochDay(),
                    topicProgress = current.topicProgress
                        .filterNot { it.topicId == topicId } + next,
                )
            }
        }
    }

    fun addPlanTask(
        title: String,
        subject: Subject?,
        period: PlanPeriod,
        metric: PlanMetric,
        target: Int,
        date: LocalDate,
    ) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val anchor = if (period == PlanPeriod.WEEKLY) {
                date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            } else {
                date
            }
            repository.update { current ->
                current.withActivity(today).copy(
                    planTasks = current.planTasks + PlanTask(
                        id = UUID.randomUUID().toString(),
                        title = title.trim().ifBlank { "Çalışma" },
                        subject = subject,
                        period = period,
                        metric = metric,
                        target = target.coerceAtLeast(1),
                        date = anchor,
                    ),
                )
            }
        }
    }

    fun togglePlanTask(id: String) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                current.withActivity(today).copy(
                    planTasks = current.planTasks.map { task ->
                        if (task.id == id) task.copy(completed = !task.completed) else task
                    },
                )
            }
        }
    }

    fun deletePlanTask(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(planTasks = current.planTasks.filterNot { it.id == id })
            }
        }
    }

    fun carryOverPlanTasks() {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                current.copy(
                    planTasks = current.planTasks.map { task ->
                        if (task.period == PlanPeriod.DAILY &&
                            !task.completed &&
                            task.date.isBefore(today)
                        ) {
                            task.copy(date = today, carriedFrom = task.date)
                        } else {
                            task
                        }
                    },
                )
            }
        }
    }

    fun toggleRestDay(date: LocalDate) {
        viewModelScope.launch {
            repository.update { current ->
                val epoch = date.toEpochDay()
                val next = if (epoch in current.restDayEpochDays) {
                    current.restDayEpochDays - epoch
                } else {
                    current.restDayEpochDays + epoch
                }
                current.copy(restDayEpochDays = next)
            }
        }
    }

    fun applyPlanSuggestions(topics: List<Topic>) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                val existingTitles = current.planTasks
                    .filter { !it.completed && it.date == today }
                    .map { it.title }
                    .toSet()
                val additions = current.planSuggestions(topics, today)
                    .filterNot { it.title in existingTitles }
                    .map { suggestion ->
                        PlanTask(
                            id = UUID.randomUUID().toString(),
                            title = suggestion.title,
                            subject = suggestion.subject,
                            period = PlanPeriod.DAILY,
                            metric = suggestion.metric,
                            target = suggestion.target,
                            date = today,
                        )
                    }
                current.withActivity(today).copy(planTasks = current.planTasks + additions)
            }
        }
    }

    fun addExamResult(
        exam: ExamType,
        sections: List<ExamSectionResult>,
        durationMinutes: Int,
        scheduleAnalysisReminder: Boolean,
    ) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val totalNet = sections.sumOf { it.net }
            val id = UUID.randomUUID().toString()
            repository.update { current ->
                current.withActivity(today).copy(
                    nets = current.nets + ExamNet(
                        id = id,
                        date = today,
                        exam = exam,
                        net = totalNet,
                        sections = sections,
                        durationMinutes = durationMinutes.coerceAtLeast(0),
                    ),
                )
            }
            if (scheduleAnalysisReminder) {
                ExamAnalysisScheduler.schedule(getApplication(), id)
            }
        }
    }

    fun saveExamAnalysis(
        examId: String,
        note: String,
        lossTopics: String,
        scheduleReminder: Boolean,
    ) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    nets = current.nets.map { exam ->
                        if (exam.id == examId) {
                            exam.copy(
                                analysisNote = note.trim(),
                                lossTopics = lossTopics.trim(),
                                analysisCompleted = true,
                            )
                        } else {
                            exam
                        }
                    },
                )
            }
            if (scheduleReminder) {
                ExamAnalysisScheduler.schedule(getApplication(), examId)
            }
        }
    }

    fun setExamDate(date: LocalDate) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(profile = current.profile.copy(examDate = date))
            }
        }
    }

    fun setStudyGoals(
        dailyQuestions: Int,
        dailyMinutes: Int,
        weeklyQuestions: Int,
        weeklyMinutes: Int,
    ) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    profile = current.profile.copy(
                        dailyQuestionTarget = dailyQuestions.coerceIn(1, 2000),
                        dailyMinuteTarget = dailyMinutes.coerceIn(1, 1440),
                        weeklyQuestionTarget = weeklyQuestions.coerceIn(1, 10000),
                        weeklyMinuteTarget = weeklyMinutes.coerceIn(1, 10080),
                    ),
                )
            }
        }
    }

    fun addNetGoal(exam: ExamType, subject: Subject?, targetNet: Double) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.update { current ->
                current.copy(
                    netGoals = current.netGoals + NetGoal(
                        id = UUID.randomUUID().toString(),
                        exam = exam,
                        subject = subject,
                        targetNet = targetNet.coerceIn(0.0, exam.maxNet),
                        year = today.year,
                        month = today.monthValue,
                    ),
                )
            }
        }
    }

    fun deleteNetGoal(id: String) {
        viewModelScope.launch {
            repository.update { current ->
                current.copy(netGoals = current.netGoals.filterNot { it.id == id })
            }
        }
    }

    fun replaceAll(data: UserData) {
        viewModelScope.launch {
            repository.replaceAll(data)
            if (data.profile.reminderEnabled) {
                reminders.schedule(
                    getApplication(),
                    data.profile.reminderHour,
                    data.profile.reminderMinute,
                )
            } else {
                reminders.cancel(getApplication())
            }
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            reminders.cancel(getApplication())
            ExamAnalysisScheduler.cancelAll(getApplication())
            StudyTimer.discard(getApplication())
            repository.clear()
        }
    }
}
