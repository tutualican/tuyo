package io.github.tutualican.tuyo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.tutualican.tuyo.data.ReminderScheduler
import io.github.tutualican.tuyo.data.UserRepository
import io.github.tutualican.tuyo.domain.ReviewOutcome
import io.github.tutualican.tuyo.domain.ReviewSchedule
import io.github.tutualican.tuyo.domain.model.ExamNet
import io.github.tutualican.tuyo.domain.model.ExamTrack
import io.github.tutualican.tuyo.domain.model.ExamType
import io.github.tutualican.tuyo.domain.model.Mistake
import io.github.tutualican.tuyo.domain.model.UserData
import io.github.tutualican.tuyo.domain.model.UserProfile
import io.github.tutualican.tuyo.domain.model.withReview
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
                    .copy(reviewsCompleted = current.reviewsCompleted + 1)
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

    fun resetAll() {
        viewModelScope.launch {
            reminders.cancel(getApplication())
            repository.clear()
        }
    }
}
