package com.nonhlanhla.endlesscalendar.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nonhlanhla.endlesscalendar.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getLong(KEY_EVENT_ID, -1L)
        val occurrenceStart = inputData.getLong(KEY_OCCURRENCE_START, -1L)
        if (eventId < 0) return Result.failure()

        val event = AppDatabase.getInstance(applicationContext).eventDao().getById(eventId)
            ?: return Result.success()

        val timeText = if (occurrenceStart > 0) {
            SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault()).format(occurrenceStart)
        } else ""

        NotificationHelper.show(
            applicationContext,
            eventId,
            event.title,
            if (timeText.isNotBlank()) timeText else event.location
        )
        return Result.success()
    }

    companion object {
        const val KEY_EVENT_ID = "eventId"
        const val KEY_OCCURRENCE_START = "occurrenceStart"
    }
}
