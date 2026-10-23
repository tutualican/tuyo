package io.github.tutualican.tuyo.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ExamAnalysisScheduler {
    private const val KEY_EXAM_ID = "exam_id"
    private const val TAG = "tuyo-exam-analysis"

    fun schedule(context: Context, examId: String) {
        val now = LocalDateTime.now()
        var next = LocalDate.now().plusDays(1).atTime(20, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = Duration.between(now, next).toMillis().coerceAtLeast(1_000L)
        val request = OneTimeWorkRequestBuilder<ExamAnalysisWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_EXAM_ID to examId))
            .addTag(TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "tuyo-exam-analysis-$examId",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
    }

    fun examId(data: androidx.work.Data): String = data.getString(KEY_EXAM_ID).orEmpty()
}
