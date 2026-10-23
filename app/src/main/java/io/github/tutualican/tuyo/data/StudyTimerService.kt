package io.github.tutualican.tuyo.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import io.github.tutualican.tuyo.MainActivity
import io.github.tutualican.tuyo.R
import io.github.tutualican.tuyo.domain.model.StudySession
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.TimerMode
import io.github.tutualican.tuyo.domain.withActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class StudyTimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var ticker: Job? = null
    private var elapsedSeconds: Int = 0
    private var targetSeconds: Int = 0
    private var mode: TimerMode = TimerMode.FREE
    private var subject: Subject? = null
    private var stopping = false
    private var sessionSaved = false
    private var runToken = 0

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                mode = runCatching {
                    TimerMode.valueOf(intent.getStringExtra(EXTRA_MODE).orEmpty())
                }.getOrDefault(TimerMode.FREE)
                subject = intent.getStringExtra(EXTRA_SUBJECT)
                    ?.let { name -> Subject.entries.firstOrNull { it.name == name } }
                targetSeconds = intent.getIntExtra(EXTRA_TARGET_SECONDS, 0)
                    .coerceAtLeast(0)
                elapsedSeconds = 0
                stopping = false
                sessionSaved = false
                runToken += 1
                startForegroundCompat(buildNotification())
                startTicker()
            }
            ACTION_PAUSE -> {
                ticker?.cancel()
                StudyTimer.update(
                    StudyTimer.state.value.copy(
                        elapsedSeconds = elapsedSeconds,
                        running = false,
                    ),
                )
                updateNotification()
            }
            ACTION_RESUME -> {
                if (!stopping) {
                    StudyTimer.update(StudyTimer.state.value.copy(running = true, finished = false))
                    startTicker()
                    updateNotification()
                }
            }
            ACTION_STOP -> stopAndSave(completed = false)
            ACTION_DISCARD -> {
                stopping = true
                runToken += 1
                ticker?.cancel()
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                StudyTimer.reset()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        ticker?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                delay(1_000L)
                if (!StudyTimer.state.value.running || stopping) break
                elapsedSeconds += 1
                val state = StudyTimer.state.value.copy(
                    elapsedSeconds = elapsedSeconds,
                    running = true,
                )
                StudyTimer.update(state)
                updateNotification()
                if (targetSeconds > 0 && elapsedSeconds >= targetSeconds) {
                    stopAndSave(completed = true)
                    break
                }
            }
        }
    }

    private fun stopAndSave(completed: Boolean) {
        if (stopping) return
        stopping = true
        ticker?.cancel()
        val token = runToken
        val snapshot = elapsedSeconds
        val sessionSubject = subject
        val sessionMode = mode
        if (completed) {
            StudyTimer.update(
                StudyTimer.state.value.copy(
                    elapsedSeconds = snapshot,
                    running = false,
                    finished = true,
                ),
            )
        } else {
            StudyTimer.reset()
        }
        if (snapshot >= 30 && !sessionSaved) {
            sessionSaved = true
            scope.launch {
                saveSession(snapshot, sessionSubject, sessionMode)
                if (token == runToken) {
                    finishService(completed, sessionMode, snapshot)
                }
            }
        } else {
            finishService(completed, sessionMode, snapshot)
        }
    }

    private suspend fun saveSession(
        seconds: Int,
        sessionSubject: Subject?,
        sessionMode: TimerMode,
    ) {
        val today = LocalDate.now()
        val minutes = (seconds / 60).coerceAtLeast(1)
        runCatching {
            UserRepository(applicationContext).update { current ->
                current.withActivity(today).copy(
                    studySessions = current.studySessions + StudySession(
                        id = UUID.randomUUID().toString(),
                        date = today,
                        subject = sessionSubject,
                        minutes = minutes,
                        mode = sessionMode,
                    ),
                )
            }
        }
    }

    private fun finishService(
        completed: Boolean,
        finishedMode: TimerMode,
        finishedSeconds: Int,
    ) {
        if (completed && ReminderScheduler.canNotify(this)) {
            ReminderScheduler.show(
                this,
                "Süre tamam",
                if (finishedMode == TimerMode.EXAM) {
                    "Sınav simülasyonu bitti. Nefes al ve analiz et."
                } else {
                    "${minutesLabel(finishedSeconds)} çalışma kaydedildi. Küçük bir mola iyi gelir."
                },
            )
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundCompat(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val canPost = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (!canPost) return
        runCatching {
            NotificationManagerCompat.from(this)
                .notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun buildNotification(): android.app.Notification {
        val state = StudyTimer.state.value
        val action = if (state.running) ACTION_PAUSE else ACTION_RESUME
        val actionLabel = if (state.running) "Duraklat" else "Devam"
        val toggle = PendingIntent.getService(
            this,
            1,
            Intent(this, StudyTimerService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this,
            2,
            Intent(this, StudyTimerService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val open = PendingIntent.getActivity(
            this,
            3,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val body = buildString {
            if (mode == TimerMode.POMODORO) append("Pomodoro · ")
            if (mode == TimerMode.EXAM) append("Sınav simülasyonu · ")
            subject?.let { append("${it.label} · ") }
            append(
                if (targetSeconds > 0) {
                    "${formatDuration((targetSeconds - elapsedSeconds).coerceAtLeast(0))} kaldı"
                } else {
                    formatDuration(elapsedSeconds)
                },
            )
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Çalışma sayacı")
            .setContentText(body)
            .setContentIntent(open)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(
                if (targetSeconds > 0) 100 else 0,
                if (targetSeconds > 0) {
                    (elapsedSeconds * 100 / targetSeconds).coerceIn(0, 100)
                } else {
                    0
                },
                false,
            )
            .addAction(0, actionLabel, toggle)
            .addAction(0, "Bitir", stop)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Çalışma sayacı",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Pomodoro, serbest sayaç ve sınav simülasyonu"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun formatDuration(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    private fun minutesLabel(seconds: Int): String {
        val minutes = (seconds / 60).coerceAtLeast(1)
        return "$minutes dk"
    }

    companion object {
        const val ACTION_START = "io.github.tutualican.tuyo.timer.START"
        const val ACTION_PAUSE = "io.github.tutualican.tuyo.timer.PAUSE"
        const val ACTION_RESUME = "io.github.tutualican.tuyo.timer.RESUME"
        const val ACTION_STOP = "io.github.tutualican.tuyo.timer.STOP"
        const val ACTION_DISCARD = "io.github.tutualican.tuyo.timer.DISCARD"
        const val EXTRA_MODE = "mode"
        const val EXTRA_SUBJECT = "subject"
        const val EXTRA_TARGET_SECONDS = "target_seconds"
        private const val CHANNEL_ID = "tuyo_timer"
        private const val NOTIFICATION_ID = 4102
    }
}
