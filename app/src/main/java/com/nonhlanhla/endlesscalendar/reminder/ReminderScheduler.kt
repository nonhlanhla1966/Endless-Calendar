package com.nonhlanhla.endlesscalendar.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nonhlanhla.endlesscalendar.data.Event
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private fun workName(eventId: Long, offsetMinutes: Int) = "reminder-$eventId-$offsetMinutes"

    fun scheduleForEvent(context: Context, event: Event) {
        cancelForEvent(context, event.id)
        val offsets = event.reminderMinutes.split(",").mapNotNull { it.trim().toIntOrNull() }
        val now = System.currentTimeMillis()
        val workManager = WorkManager.getInstance(context)

        for (offset in offsets) {
            val triggerAt = event.startMillis - offset * 60_000L
            val delay = triggerAt - now
            if (delay <= 0) continue

            val data = Data.Builder()
                .putLong(ReminderWorker.KEY_EVENT_ID, event.id)
                .putLong(ReminderWorker.KEY_OCCURRENCE_START, event.startMillis)
                .build()

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("event-${event.id}")
                .build()

            workManager.enqueueUniqueWork(workName(event.id, offset), ExistingWorkPolicy.REPLACE, request)
        }
    }

    fun cancelForEvent(context: Context, eventId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("event-$eventId")
    }

    /** Re-arms reminders for all upcoming events, used after a reboot clears WorkManager alarms. */
    fun rescheduleAll(context: Context, events: List<Event>) {
        val now = System.currentTimeMillis()
        events.filter { it.startMillis > now && it.reminderMinutes.isNotBlank() }
            .forEach { scheduleForEvent(context, it) }
    }
}
