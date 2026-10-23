package io.github.tutualican.tuyo.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.github.tutualican.tuyo.domain.model.Subject
import io.github.tutualican.tuyo.domain.model.TimerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StudyTimerState(
    val mode: TimerMode = TimerMode.FREE,
    val subject: Subject? = null,
    val targetSeconds: Int = 0,
    val elapsedSeconds: Int = 0,
    val running: Boolean = false,
    val finished: Boolean = false,
) {
    val remainingSeconds: Int
        get() = if (targetSeconds > 0) {
            (targetSeconds - elapsedSeconds).coerceAtLeast(0)
        } else {
            0
        }

    val progress: Float
        get() = if (targetSeconds > 0) {
            (elapsedSeconds.toFloat() / targetSeconds).coerceIn(0f, 1f)
        } else {
            0f
        }
}

/**
 * Process-wide timer state. The foreground service owns the ticking clock; the
 * UI observes this flow and survives navigation while the app is in background.
 */
object StudyTimer {
    private val _state = MutableStateFlow(StudyTimerState())
    val state: StateFlow<StudyTimerState> = _state.asStateFlow()

    fun start(
        context: Context,
        mode: TimerMode,
        subject: Subject?,
        targetMinutes: Int,
    ) {
        val targetSeconds = targetMinutes.coerceAtLeast(0) * 60
        _state.value = StudyTimerState(
            mode = mode,
            subject = subject,
            targetSeconds = targetSeconds,
            elapsedSeconds = 0,
            running = true,
        )
        send(
            context,
            Intent(context, StudyTimerService::class.java).apply {
                action = StudyTimerService.ACTION_START
                putExtra(StudyTimerService.EXTRA_MODE, mode.name)
                putExtra(StudyTimerService.EXTRA_SUBJECT, subject?.name)
                putExtra(StudyTimerService.EXTRA_TARGET_SECONDS, targetSeconds)
            },
        )
    }

    fun pause(context: Context) = sendAction(
        context,
        Intent(context, StudyTimerService::class.java).setAction(StudyTimerService.ACTION_PAUSE),
    )

    fun resume(context: Context) = sendAction(
        context,
        Intent(context, StudyTimerService::class.java).setAction(StudyTimerService.ACTION_RESUME),
    )

    fun stop(context: Context) {
        sendAction(
            context,
            Intent(context, StudyTimerService::class.java).setAction(StudyTimerService.ACTION_STOP),
        )
        // The service keeps its own snapshot; clearing here makes an ended
        // session disappear immediately even while its save is still running.
        reset()
    }

    /** Drops the active session without saving it (used by a full data reset). */
    fun discard(context: Context) = sendAction(
        context,
        Intent(context, StudyTimerService::class.java).setAction(StudyTimerService.ACTION_DISCARD),
    )

    /** Clears the UI state after the service has saved/stopped a session. */
    fun reset() {
        _state.value = StudyTimerState()
    }

    internal fun update(state: StudyTimerState) {
        _state.value = state
    }

    private fun send(context: Context, intent: Intent) {
        runCatching { ContextCompat.startForegroundService(context, intent) }
    }

    /**
     * Pause/resume/stop only address a service that is already running. Using a
     * plain service start here avoids a foreground-service deadline when the UI
     * asks a finished timer to stop.
     */
    private fun sendAction(context: Context, intent: Intent) {
        runCatching { context.startService(intent) }
    }
}
