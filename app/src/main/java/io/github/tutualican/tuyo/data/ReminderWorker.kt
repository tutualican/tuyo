package io.github.tutualican.tuyo.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.tutualican.tuyo.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val state = UserRepository(applicationContext).read()
        val profile = state.profile
        if (!profile.onboardingComplete || !profile.reminderEnabled) {
            return Result.success()
        }

        val today = LocalDate.now()
        val isRestDay = today.toEpochDay() in state.restDayEpochDays
        if (ReminderScheduler.canNotify(applicationContext) && !isRestDay) {
            val due = state.mistakes.count { it.isDue(today) }
            val pendingPlan = state.planTasks.count {
                !it.completed && it.date <= today &&
                    it.period == io.github.tutualican.tuyo.domain.model.PlanPeriod.DAILY
            }
            val idleDays = state.lastActivityEpochDay
                ?.let { ChronoUnit.DAYS.between(LocalDate.ofEpochDay(it), today) }
            val (title, body) = when {
                state.mistakes.isEmpty() && pendingPlan == 0 -> {
                    applicationContext.getString(R.string.notification_empty_title) to
                        applicationContext.getString(R.string.notification_empty_body)
                }
                due > 0 -> {
                    applicationContext.getString(R.string.notification_due_title) to
                        applicationContext.getString(R.string.notification_due_body, due)
                }
                pendingPlan > 0 -> {
                    applicationContext.getString(R.string.notification_plan_title) to
                        applicationContext.getString(R.string.notification_plan_body, pendingPlan)
                }
                idleDays != null && idleDays >= 2 -> {
                    applicationContext.getString(R.string.notification_idle_title) to
                        applicationContext.getString(R.string.notification_idle_body)
                }
                else -> {
                    applicationContext.getString(R.string.notification_done_title) to
                        applicationContext.getString(R.string.notification_done_body)
                }
            }
            ReminderScheduler.show(applicationContext, title, body)
        }

        // A worker is one-shot, so it owns the next day's hand-off. This keeps
        // the schedule alive across process death and device reboots.
        ReminderScheduler.schedule(
            applicationContext,
            profile.reminderHour,
            profile.reminderMinute,
        )
        return Result.success()
    }
}
