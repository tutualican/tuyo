package io.github.tutualican.tuyo.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.tutualican.tuyo.R

class ExamAnalysisWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val examId = ExamAnalysisScheduler.examId(inputData)
        if (examId.isBlank()) return Result.success()
        val state = UserRepository(applicationContext).read()
        val exam = state.nets.firstOrNull { it.id == examId } ?: return Result.success()
        if (ReminderScheduler.canNotify(applicationContext)) {
            val reviewed = exam.analysisCompleted
            ReminderScheduler.show(
                applicationContext,
                applicationContext.getString(
                    if (reviewed) R.string.notification_analysis_review_title
                    else R.string.notification_analysis_title,
                ),
                applicationContext.getString(
                    if (reviewed) R.string.notification_analysis_review_body
                    else R.string.notification_analysis_body,
                    exam.exam.label,
                ),
            )
        }
        return Result.success()
    }
}
