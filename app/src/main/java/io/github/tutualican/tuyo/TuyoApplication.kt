package io.github.tutualican.tuyo

import android.app.Application
import io.github.tutualican.tuyo.data.ReminderScheduler

/**
 * The only process-level setup is the notification channel. Scheduling is
 * owned by [ReminderScheduler] and survives process death through WorkManager.
 */
class TuyoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.createChannel(this)
    }
}
